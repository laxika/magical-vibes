package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FiresOfYavimaya;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NaturalEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control become 2/2 creatures with first strike")
    void animatesOnlyControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturalEmergence());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();

        Permanent mountain = findPermanent(player2, "Mountain");
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Enters with a non-targeting choice to return a red or green enchantment")
    void etbChoosesMatchingEnchantment() {
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturalEmergence()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 4);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        UUID firesId = harness.getPermanentId(player1, "Fires of Yavimaya");

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID emergenceId = harness.getPermanentId(player1, "Natural Emergence");
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firesId, emergenceId);

        harness.handlePermanentChosen(player1, firesId);

        harness.assertInHand(player1, "Fires of Yavimaya");
        harness.assertOnBattlefield(player1, "Natural Emergence");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }
}
