package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OkinaTempleToTheGrandfathers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeastOfWorms.class, Mountain.class, Forest.class, Plains.class,
        OkinaTempleToTheGrandfathers.class, KamiOfOldStone.class})
class FeastOfWormsTest extends BaseCardTest {

    private Permanent legendaryLand(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new OkinaTempleToTheGrandfathers());
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new FeastOfWorms()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 0, targetId);
    }

    @Test
    @DisplayName("Destroys a nonlegendary land with no extra sacrifice")
    void destroysNonlegendaryLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        castAt(harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Legendary land: its controller sacrifices another land of their choice")
    void legendaryLandForcesSacrificeChoice() {
        Permanent legendary = legendaryLand(player2);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        castAt(legendary.getId());

        harness.assertNotOnBattlefield(player2, "Okina, Temple to the Grandfathers");

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(choice.maxCount()).isEqualTo(1);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.handleMultiplePermanentsChosen(player2, List.of(forestId));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Legendary land with exactly one other land: that land is sacrificed with no prompt")
    void singleRemainingLandIsSacrificedAutomatically() {
        Permanent legendary = legendaryLand(player2);
        harness.addToBattlefield(player2, new Forest());

        castAt(legendary.getId());

        harness.assertNotOnBattlefield(player2, "Okina, Temple to the Grandfathers");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Legendary land with no other land: nothing else is sacrificed")
    void noOtherLandLeavesNothingToSacrifice() {
        Permanent legendary = legendaryLand(player2);
        harness.addToBattlefield(player2, new KamiOfOldStone());

        castAt(legendary.getId());

        harness.assertNotOnBattlefield(player2, "Okina, Temple to the Grandfathers");
        harness.assertOnBattlefield(player2, "Kami of Old Stone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The sacrifice hits the destroyed land's controller, not the caster")
    void sacrificeGoesToTargetsController() {
        Permanent legendary = legendaryLand(player2);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new Plains());

        castAt(legendary.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new KamiOfOldStone());
        harness.setHand(player1, List.of(new FeastOfWorms()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID kamiId = harness.getPermanentId(player2, "Kami of Old Stone");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, kamiId))
                .isInstanceOf(IllegalStateException.class);
    }
}
