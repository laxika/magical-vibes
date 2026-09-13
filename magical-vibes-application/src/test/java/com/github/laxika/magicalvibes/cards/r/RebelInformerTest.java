package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.e.Excise;
import com.github.laxika.magicalvibes.cards.m.MineBearer;
import com.github.laxika.magicalvibes.cards.s.SearingWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebelInformer.class, DivingGriffin.class, Excise.class, MineBearer.class, SearingWind.class})
class RebelInformerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nontoken Rebel on the bottom of its owner's library")
    void putsNontokenRebelOnBottomOfLibrary() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        Permanent rebel = addCreatureReady(player2, new RebelInformer());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, rebel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(informer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rebel);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).last().isSameAs(rebel.getCard());
    }

    @Test
    @DisplayName("Cannot target a non-Rebel permanent")
    void cannotTargetNonRebelPermanent() {
        addCreatureReady(player1, new RebelInformer());
        Permanent griffin = addCreatureReady(player2, new DivingGriffin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, griffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Rebel");
    }

    @Test
    @DisplayName("Cannot target a token Rebel")
    void cannotTargetTokenRebel() {
        addCreatureReady(player1, new RebelInformer());
        RebelInformer tokenRebelCard = new RebelInformer();
        tokenRebelCard.setToken(true);
        Permanent tokenRebel = addCreatureReady(player2, tokenRebelCard);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tokenRebel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Rebel");
    }

    @Test
    @DisplayName("Puts a controlled Rebel on the bottom of its owner's library")
    void putsControlledRebelOnBottomOfOwnersLibrary() {
        addCreatureReady(player1, new RebelInformer());
        RebelInformer ownedByPlayer1 = new RebelInformer();
        ownedByPlayer1.setOwnerId(player1.getId());
        Permanent rebel = addCreatureReady(player2, ownedByPlayer1);
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, rebel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rebel);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore + 1)
                .last().isSameAs(ownedByPlayer1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSizeBefore);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        informer.setAttacking(true);
        informer.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new Excise()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 0, List.of(informer.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability from a white source")
    void cannotBeTargetedByWhiteSourceAbility() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        informer.setAttacking(true);
        informer.setAttackTarget(player1.getId());
        Permanent mineBearer = addCreatureReady(player2, new MineBearer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        int mineBearerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(mineBearer);

        assertThatThrownBy(() -> harness.activateAbility(player2, mineBearerIndex, 0, null,
                informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }

    @Test
    @DisplayName("Cannot be targeted by a white ability from its controller")
    void cannotBeTargetedByOwnWhiteSourceAbility() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        informer.setAttacking(true);
        informer.setAttackTarget(player2.getId());
        Permanent mineBearer = addCreatureReady(player1, new MineBearer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        int mineBearerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mineBearer);

        assertThatThrownBy(() -> harness.activateAbility(player1, mineBearerIndex, 0, null,
                informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }

    @Test
    @DisplayName("Can be targeted by a nonwhite spell")
    void canBeTargetedByNonwhiteSpell() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        SearingWind spell = new SearingWind();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.castInstant(player2, 0, informer.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }
}
