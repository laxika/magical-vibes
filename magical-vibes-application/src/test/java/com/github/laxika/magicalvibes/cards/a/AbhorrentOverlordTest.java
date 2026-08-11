package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
