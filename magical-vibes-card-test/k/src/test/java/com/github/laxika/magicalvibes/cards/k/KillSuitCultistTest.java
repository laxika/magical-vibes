package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Phytohydra;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KillSuitCultist.class, GiantSpider.class, GrizzlyBears.class, Shock.class, Phytohydra.class})
class KillSuitCultistTest extends BaseCardTest {

    @Test
    void mustAttackEachCombatIfAble() {
        addCreatureReady(player1, new KillSuitCultist());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void tappedCultistIsNotRequiredToAttack() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        cultist.tap();

        declareAttackers(List.of());
        assertThat(cultist.isAttacking()).isFalse();
    }

    @Test
    void summoningSickCultistCanActivateAbility() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        cultist.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Kill-Suit Cultist");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void abilityOnlyTargetsCreatures() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());

        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cultist), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cultist);
    }

    @Test
    void sacrificesItselfAndDestroysCreatureInsteadOfNextDamage() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cultist), null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cultist);

        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void replacementOnlyAppliesToTheNextDamageToTheTarget() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GiantSpider());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cultist), null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void replacementAlsoAppliesToCombatDamage() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cultist), null,
                target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void damageBeforeAbilityResolvesDoesNotConsumeReplacement() {
        Permanent cultist = addCreatureReady(player1, new KillSuitCultist());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cultist);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);

        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void unusedReplacementExpiresAtEndOfTurn() {
        addCreatureReady(player1, new KillSuitCultist());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void targetControllerChoosesBetweenCompetingDamageReplacements() {
        addCreatureReady(player1, new KillSuitCultist());
        Permanent target = addCreatureReady(player2, new Phytohydra());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Destroying instead of Shock's damage asks library-or-Kalitas before the Zombie leaves")
    void destructionInsteadOfSpellDamageAsksTheDeathReplacementFirst() {
        addCreatureReady(player1, new KillSuitCultist());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();
        Card shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(shock.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(zombie.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
        assertThat(zombie.getMarkedDamage()).isZero();
        assertThat(tokens(player1)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Destroying instead of combat damage asks library-or-Kalitas before the blocker leaves")
    void destructionInsteadOfCombatDamageAsksTheDeathReplacementFirst() {
        addCreatureReady(player1, new KillSuitCultist());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0,
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(zombie.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(zombie.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(tokens(player1)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private long tokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
