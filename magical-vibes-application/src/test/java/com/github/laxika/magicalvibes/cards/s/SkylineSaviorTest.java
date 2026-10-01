package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkylineSavior.class, GrizzlyBears.class, Island.class, SerraAngel.class})
class SkylineSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets you choose any permanent you control")
    void etbChoosesOwnPermanent() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        UUID opponentBearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castAndResolveSkylineSavior();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(islandId)
                .doesNotContain(opponentBearsId);
    }

    @Test
    @DisplayName("A returned non-Angel creature card perpetually gets the stated upgrade")
    void upgradesReturnedNonAngelCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAndResolveSkylineSavior();
        harness.handlePermanentChosen(player1, bears.getId());

        Card returnedBears = bears.getCard();
        harness.setHand(player1, List.of(returnedBears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedBears)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, recast, Keyword.FLYING)).isTrue();
        assertThat(gqs.cardHasSubtype(returnedBears, CardSubtype.ANGEL, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("A returned Angel creature card does not receive the upgrade")
    void doesNotUpgradeReturnedAngel() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        castAndResolveSkylineSavior();
        harness.handlePermanentChosen(player1, angel.getId());

        Card returnedAngel = angel.getCard();
        harness.setHand(player1, List.of(returnedAngel));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedAngel)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(4);
    }

    private void castAndResolveSkylineSavior() {
        harness.setHand(player1, List.of(new SkylineSavior()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
