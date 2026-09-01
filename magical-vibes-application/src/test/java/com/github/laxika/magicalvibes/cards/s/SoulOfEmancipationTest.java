package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfEmancipation.class, GrizzlyBears.class, Island.class})
class SoulOfEmancipationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys up to three other nonland permanents and gives their controllers Angels")
    void destroysPermanentsAndCreatesAngelsForTheirControllers() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSoul(List.of(ownPermanent.getId(), firstOpponentPermanent.getId(), secondOpponentPermanent.getId()));

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Soul of Emancipation")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).hasSize(2);
        assertThat(findPermanents(player1, "Angel")).allSatisfy(this::assertAngel);
        assertThat(findPermanents(player2, "Angel")).allSatisfy(this::assertAngel);
    }

    @Test
    @DisplayName("The source and lands cannot be chosen")
    void sourceAndLandsAreNotLegalTargets() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SoulOfEmancipation()));
        addManaForSoul();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soul of Emancipation")).hasSize(1);
        assertThat(findPermanents(player1, "Island")).hasSize(1);
    }

    private void castSoul(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SoulOfEmancipation()));
        addManaForSoul();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addManaForSoul() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void assertAngel(Permanent angel) {
        assertThat(angel.getCard().getPower()).isEqualTo(3);
        assertThat(angel.getCard().getToughness()).isEqualTo(3);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(angel.getCard().isToken()).isTrue();
    }
}
