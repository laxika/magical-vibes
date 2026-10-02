package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.d.DevoutHarpist;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelsTrumpet.class, Crawlspace.class, DevoutHarpist.class, IvoryMask.class})
class AngelsTrumpetTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have vigilance")
    void grantsVigilanceToAllCreatures() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent ownCreature = addCreatureReady(player1, new DevoutHarpist());
        Permanent opposingCreature = addCreatureReady(player2, new DevoutHarpist());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("At each end step, taps that player's untapped creatures that did not attack and deals equal damage")
    void tapsNonAttackingCreaturesAndDamagesActivePlayer() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent nonAttacker = addCreatureReady(player1, new DevoutHarpist());
        Permanent attacker = addCreatureReady(player1, new DevoutHarpist());
        attacker.setAttackedThisTurn(true);
        Permanent alreadyTapped = addCreatureReady(player1, new DevoutHarpist());
        alreadyTapped.tap();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(nonAttacker.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(alreadyTapped.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger affects the active player on an opponent's end step")
    void affectsOpponentAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent ownCreature = addCreatureReady(player1, new DevoutHarpist());
        Permanent opposingCreature = addCreatureReady(player2, new DevoutHarpist());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage equal to the number of creatures it taps")
    void dealsDamageForEachCreatureTapped() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent firstNonAttacker = addCreatureReady(player1, new DevoutHarpist());
        Permanent secondNonAttacker = addCreatureReady(player1, new DevoutHarpist());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(firstNonAttacker.isTapped()).isTrue();
        assertThat(secondNonAttacker.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not deal damage when no untapped creature stayed home")
    void doesNotDamageWhenNoCreatureCanBeTapped() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent attacker = addCreatureReady(player1, new DevoutHarpist());
        attacker.setAttackedThisTurn(true);
        Permanent alreadyTapped = addCreatureReady(player1, new DevoutHarpist());
        alreadyTapped.tap();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(alreadyTapped.isTapped()).isTrue();
        assertThat(noncreature.isTapped()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Shroud does not stop the non-targeting end-step ability")
    void affectsPlayerWithShroud() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        harness.addToBattlefield(player2, new IvoryMask());
        Permanent creature = addCreatureReady(player2, new DevoutHarpist());
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Summoning-sick creatures are tapped and counted")
    void countsSummoningSickCreatures() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevoutHarpist());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Uses the creatures' tapped states at resolution")
    void checksTappedStateAtResolution() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent tappedInResponse = addCreatureReady(player1, new DevoutHarpist());
        Permanent untappedInResponse = addCreatureReady(player1, new DevoutHarpist());
        untappedInResponse.tap();
        harness.setLife(player1, 20);

        beginEndStep(player1);
        tappedInResponse.tap();
        untappedInResponse.untap();
        resolveAllTriggers();

        assertThat(tappedInResponse.isTapped()).isTrue();
        assertThat(untappedInResponse.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The triggered ability resolves after Angel's Trumpet leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent trumpet = harness.addToBattlefieldAndReturn(player1, new AngelsTrumpet());
        Permanent creature = addCreatureReady(player1, new DevoutHarpist());
        harness.setLife(player1, 20);

        beginEndStep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(trumpet);
        gd.playerGraveyards.get(player1.getId()).add(trumpet.getCard());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Being made attacking without being declared an attacker does not exempt a creature")
    void creatureMadeAttackingWithoutDeclarationIsTapped() {
        harness.addToBattlefield(player1, new AngelsTrumpet());
        Permanent creature = addCreatureReady(player1, new DevoutHarpist());
        creature.setAttacking(true);
        creature.setAttacking(false);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private void advanceToEndStep(Player activePlayer) {
        beginEndStep(activePlayer);
        resolveAllTriggers();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
