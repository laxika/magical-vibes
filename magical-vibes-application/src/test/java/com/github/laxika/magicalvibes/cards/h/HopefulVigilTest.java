package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HopefulVigil.class)
class HopefulVigilTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 2/2 white Knight token with vigilance")
    void entersCreatesKnightToken() {
        castAndResolve();

        assertThat(findPermanents(player1, "Knight"))
                .singleElement()
                .satisfies(knight -> {
                    assertThat(knight.getCard().isToken()).isTrue();
                    assertThat(knight.getCard().getPower()).isEqualTo(2);
                    assertThat(knight.getCard().getToughness()).isEqualTo(2);
                    assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
                });
    }

    @Test
    @DisplayName("Scry 2 triggers when it is put into a graveyard from the battlefield")
    void scriesWhenPutIntoGraveyardFromBattlefield() {
        Permanent vigil = new Permanent(new HopefulVigil());
        gd.playerBattlefields.get(player1.getId()).add(vigil);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vigil));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Paying {2}{W} sacrifices it")
    void sacrificeAbilitySacrificesIt() {
        castAndResolve();
        Permanent vigil = findPermanent(player1, "Hopeful Vigil");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vigil), 0, null, null);

        assertThat(findPermanents(player1, "Hopeful Vigil")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Hopeful Vigil");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HopefulVigil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
