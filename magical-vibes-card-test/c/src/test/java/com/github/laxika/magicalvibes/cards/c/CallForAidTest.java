package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallForAid.class, GrizzlyBears.class, VolrathsStronghold.class, AshnodsAltar.class, Humble.class, RayOfCommand.class})
class CallForAidTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, grants haste and protects the opponent's creatures")
    void stealsUntapsHastesAndProtectsCreatures() {
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());
        firstCreature.tap();
        secondCreature.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        castCallForAid(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstCreature, secondCreature)
                .doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, firstCreature)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, secondCreature)).isTrue();
    }

    @Test
    @DisplayName("Temporary control and creature riders expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castCallForAid(player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("The caster cannot attack the targeted player that turn")
    void casterCannotAttackTargetedPlayer() {
        addCreatureReady(player1, new GrizzlyBears());

        castCallForAid(player2.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CallForAid()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }


    @Test
    @CardUsed({AshnodsAltar.class, RayOfCommand.class})
    @DisplayName("Another controller can sacrifice a creature taken with Call for Aid")
    void anotherControllerCanSacrificeAffectedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AshnodsAltar());

        castCallForAid(player2.getId());

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @CardUsed({AshnodsAltar.class, Humble.class})
    @DisplayName("Losing abilities does not let the caster sacrifice a stolen creature")
    void losingAbilitiesDoesNotRemoveSacrificeRestriction() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castCallForAid(player2.getId());

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature.getCard());
    }

    @Test
    @CardUsed({AshnodsAltar.class})
    @DisplayName("Only the stolen creatures are excluded from sacrifice costs")
    void canSacrificeOwnCreatureButNotStolenCreature() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent stolenCreature = addCreatureReady(player2, new GrizzlyBears());

        castCallForAid(player2.getId());
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenCreature);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCallForAid(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CallForAid()));
        addMana(player1);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
