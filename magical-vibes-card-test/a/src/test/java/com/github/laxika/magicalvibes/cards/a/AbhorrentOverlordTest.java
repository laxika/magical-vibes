package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbhorrentOverlord.class})
class AbhorrentOverlordTest extends BaseCardTest {

    private List<Permanent> harpyTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Harpy"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates Harpies equal to your black devotion, including this creature")
    void etbCreatesHarpiesEqualToBlackDevotion() {
        harness.setHand(player1, List.of(new AbhorrentOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harpyTokens()).hasSize(2);
        assertThat(harpyTokens()).allSatisfy(token -> {
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Upkeep trigger makes you sacrifice a creature")
    void upkeepTriggerSacrificesCreature() {
        Permanent overlord = harness.addToBattlefieldAndReturn(player1, new AbhorrentOverlord());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AbhorrentOverlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(overlord.getId(), creature.getId());

        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(overlord.getId()));
    }

    @Test
    @DisplayName("Devotion uses current controlled permanents when the enter trigger resolves")
    void devotionIsCountedAtResolutionAndIgnoresOpponent() {
        harness.addToBattlefield(player2, new AbhorrentOverlord());
        harness.setHand(player1, List.of(new AbhorrentOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harpyTokens()).isEmpty();
        harness.addToBattlefield(player1, new AbhorrentOverlord());
        resolveAllTriggers();

        assertThat(harpyTokens()).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The Overlord sacrifices itself when it is the only creature")
    void upkeepSacrificesOverlordWhenAlone() {
        harness.addToBattlefield(player1, new AbhorrentOverlord());
        harness.addToBattlefield(player2, new AbhorrentOverlord());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AbhorrentOverlord);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Overlord does not trigger during its opponent's upkeep")
    void opponentUpkeepDoesNotRequireSacrifice() {
        Permanent overlord = harness.addToBattlefieldAndReturn(player1, new AbhorrentOverlord());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(overlord);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Harpy can be sacrificed to keep the Overlord")
    void upkeepCanSacrificeHarpyToken() {
        harness.setHand(player1, List.of(new AbhorrentOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent overlord = findPermanent(player1, "Abhorrent Overlord");
        Permanent harpy = harpyTokens().getFirst();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(overlord.getId(), harpy.getId());
        harness.handlePermanentChosen(player1, harpy.getId());

        assertThat(harpyTokens()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(overlord);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(harpy);
    }
}
