package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Haggle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfTheVale.class, Haggle.class, Forest.class, GrizzlyBears.class})
class MerchantOfTheValeTest extends BaseCardTest {

    @Test
    void haggleMayDiscardThenDrawAndExilesTheCard() {
        MerchantOfTheVale merchant = new MerchantOfTheVale();
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        harness.setHand(player1, new ArrayList<>(List.of(merchant, discarded)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.findExiledCard(merchant.getId())).isNotNull();
    }

    @Test
    void merchantAbilityDiscardsThenDraws() {
        Permanent merchant = new Permanent(new MerchantOfTheVale());
        merchant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(merchant);
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
