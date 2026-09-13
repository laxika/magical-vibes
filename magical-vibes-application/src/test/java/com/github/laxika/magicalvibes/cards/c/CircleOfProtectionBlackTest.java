package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.c.Corrupt;
import com.github.laxika.magicalvibes.cards.c.CryptRats;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oppression;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BogWraith.class,
        CircleOfProtectionBlack.class,
        Corrupt.class,
        CryptRats.class,
        GiantGrowth.class,
        GrizzlyBears.class,
        Oppression.class,
        Swamp.class
})
class CircleOfProtectionBlackTest extends BaseCardTest {

    private static final String CRYPT_RATS_MANA_COST = "{2}{B}";

    @Test
    @CardUsed({CircleOfProtectionBlack.class, BogWraith.class, GrizzlyBears.class})
    @DisplayName("Resolving the ability prompts for a black source choice")
    void resolvingAbilityPromptsForBlackSource() {
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new BogWraith());
        Permanent nonBlackSource = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(blackSource.getId()).doesNotContain(nonBlackSource.getId());
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, BogWraith.class})
    @DisplayName("Choosing a black source records a one-shot prevention shield")
    void choosingBlackSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new BogWraith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(blackSource.getId()));
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, BogWraith.class})
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new BogWraith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        blackSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, Corrupt.class, Swamp.class})
    @DisplayName("Prevents the next noncombat damage from a chosen black spell")
    void preventsNextNoncombatDamageFromChosenBlackSpell() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Corrupt blackSpell = new Corrupt();
        harness.forceActivePlayer(player2);
        harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player2, List.of(blackSpell));
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(blackSpell.getId());
        harness.handlePermanentChosen(player1, blackSpell.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, BogWraith.class})
    @DisplayName("Only the chosen source is prevented; a different black source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addCreatureReady(player2, new BogWraith());
        Permanent other = addCreatureReady(player2, new BogWraith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 3/3 deals its damage; the shield is untouched.
        harness.assertLife(player1, 17);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, CryptRats.class, GiantGrowth.class})
    @DisplayName("Prevents only the next damage event from the chosen source")
    void preventsOnlyNextDamageEventFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent rats = addCreatureReady(player2, new CryptRats());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, rats.getId());

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, rats.getId());

        int ratsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rats);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, ratsIndex, 1, null);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, ratsIndex, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, GrizzlyBears.class})
    @DisplayName("Non-black permanents are not valid source choices")
    void nonBlackSourceNotValid() {
        addReadyCircle(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, Oppression.class})
    @DisplayName("A black permanent is a valid source even when it cannot deal damage")
    void blackPermanentNeedNotDealDamage() {
        addReadyCircle(player1);
        Permanent oppression = harness.addToBattlefieldAndReturn(player2, new Oppression());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(oppression.getId());
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, BogWraith.class})
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent blackSource = addCreatureReady(player2, new BogWraith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackSource.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionBlack());
    }

    private Permanent addReadyBlackDamageSource(Player player) {
        return addCreatureReady(player, new CryptRats());
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, CryptRats.class})
    @DisplayName("Prevents the next noncombat damage from the chosen black source")
    void preventsNextNoncombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent rats = addReadyBlackDamageSource(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, rats.getId());

        harness.addMana(player2, ManaColor.BLACK, 1);
        int ratsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rats);
        harness.activateAbility(player2, ratsIndex, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, CryptRats.class, GrizzlyBears.class})
    @DisplayName("Prevention only applies to damage dealt to the protected player")
    void onlyPreventsDamageToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyCircle(player1);
        Permanent rats = harness.addToBattlefieldAndReturn(player2, new CryptRats());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, rats.getId());

        harness.addMana(player2, ManaColor.BLACK, 1);
        int ratsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rats);
        harness.activateAbility(player2, ratsIndex, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class, CryptRats.class})
    @DisplayName("A permanent spell remains the chosen source after it resolves")
    void preventsDamageFromPermanentSpellAfterItResolves() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        CryptRats cryptRatsSpell = new CryptRats();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, cryptRatsSpell, CRYPT_RATS_MANA_COST);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, cryptRatsSpell.getId());
        harness.passBothPriorities();

        Permanent rats = findPermanent(player2, "Crypt Rats");
        int ratsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rats);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, ratsIndex, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

}
