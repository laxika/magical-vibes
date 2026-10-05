package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrazingGladehart;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoragaBard.class, StoneworkPuma.class, GrazingGladehart.class, RayOfCommand.class})
class JoragaBardTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give vigilance to all Allies you control")
    void ownAllyEntryMayGrantVigilanceToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Joraga Bard");
        assertThat(existingAlly.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(enteringAlly.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(nonAlly.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering may give vigilance to all Allies you control")
    void anotherAllyEntryMayGrantVigilanceToAllAllies() {
        Permanent existingBard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringBard = findPermanents(player1, "Joraga Bard").stream()
                .filter(permanent -> !permanent.getId().equals(existingBard.getId()))
                .findFirst().orElseThrow();
        assertThat(existingBard.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(enteringBard.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no vigilance")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Joraga Bard").hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.setHand(player1, List.of(new GrazingGladehart()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bard = findPermanent(player1, "Joraga Bard");
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger the Bard")
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new StoneworkPuma()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The grant includes the entering Ally but excludes opposing Allies")
    void onlyControlledAlliesGainVigilance() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(findPermanent(player1, "Stonework Puma").hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(opposingAlly.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A Bard stolen in response does not receive its old controller's vigilance grant")
    void stolenBardIsExcludedFromPendingGrant() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.setHand(player1, List.of(new StoneworkPuma()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, bard.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bard);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Stonework Puma").hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
