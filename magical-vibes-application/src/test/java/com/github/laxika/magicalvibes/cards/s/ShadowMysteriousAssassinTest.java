package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowMysteriousAssassin.class, Forest.class, HillGiant.class})
class ShadowMysteriousAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("May sacrifice another nonland permanent to draw two and drain each opponent")
    void maySacrificeAnotherNonlandPermanent() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent shadow = addCreatureReady(player1, new ShadowMysteriousAssassin());
        Permanent sacrificed = addCreatureReady(player1, new HillGiant());
        shadow.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the sacrifice leaves Shadow and the permanent in play")
    void maySacrificeCanBeDeclined() {
        Permanent shadow = addCreatureReady(player1, new ShadowMysteriousAssassin());
        Permanent otherPermanent = addCreatureReady(player1, new HillGiant());
        shadow.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shadow, otherPermanent);
    }

    @Test
    @DisplayName("The sacrifice cannot choose a land or Shadow itself")
    void onlyAnotherNonlandPermanentCanBeSacrificed() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent shadow = addCreatureReady(player1, new ShadowMysteriousAssassin());
        Permanent land = new Permanent(new Forest());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        shadow.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shadow, land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
