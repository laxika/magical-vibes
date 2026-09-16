package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponizedScrap.class, Ornithopter.class})
class WeaponizedScrapTest extends BaseCardTest {

    @Test
    void coversTheOnlyArtifactAndKeepsBothCardsTogether() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new WeaponizedScrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(artifact);
        assertThat(artifact.getCard()).isInstanceOf(WeaponizedScrap.class);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HASTE)).isTrue();
        assertThat(artifact.cardsLeavingBattlefield().stream()
                .map(card -> card.getClass().getSimpleName())
                .toList())
                .containsExactly("WeaponizedScrap", "Ornithopter");
    }

    @Test
    void offersAChoiceAmongMultipleArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new WeaponizedScrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCard()).isInstanceOf(Ornithopter.class);
        assertThat(second.getCard()).isInstanceOf(WeaponizedScrap.class);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exilesItWhenNoArtifactCanBeCovered() {
        harness.setHand(player1, List.of(new WeaponizedScrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(WeaponizedScrap.class::isInstance);
    }
}
