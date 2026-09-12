package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AgentOfShauku;
import com.github.laxika.magicalvibes.cards.a.AvatarOfWoe;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.g.GreelsCaress;
import com.github.laxika.magicalvibes.cards.s.SearingWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercenaryInformer.class, AgentOfShauku.class, AvatarOfWoe.class,
        DivingGriffin.class, GreelsCaress.class, SearingWind.class})
class MercenaryInformerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nontoken Mercenary on the bottom of its owner's library")
    void putsNontokenMercenaryOnBottomOfLibrary() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        Permanent mercenary = addCreatureReady(player2, new AgentOfShauku());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(informer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mercenary);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).last().isSameAs(mercenary.getCard());
    }

    @Test
    @DisplayName("Cannot target a non-Mercenary permanent")
    void cannotTargetNonMercenaryPermanent() {
        addCreatureReady(player1, new MercenaryInformer());
        Permanent griffin = addCreatureReady(player2, new DivingGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, griffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Mercenary");
    }

    @Test
    @DisplayName("Cannot target a token Mercenary")
    void cannotTargetTokenMercenary() {
        addCreatureReady(player1, new MercenaryInformer());
        AgentOfShauku tokenCard = new AgentOfShauku();
        tokenCard.setToken(true);
        Permanent tokenMercenary = addCreatureReady(player2, tokenCard);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tokenMercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Mercenary");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        harness.setHand(player2, List.of(new GreelsCaress()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability from a black source")
    void cannotBeTargetedByBlackSourceAbility() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        addCreatureReady(player2, new AvatarOfWoe());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black");
    }

    @Test
    @DisplayName("Cannot be targeted by a black ability from its controller")
    void cannotBeTargetedByOwnBlackSourceAbility() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        Permanent avatar = addCreatureReady(player1, new AvatarOfWoe());

        int avatarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(avatar);
        assertThatThrownBy(() -> harness.activateAbility(player1, avatarIndex, null, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black");
    }

    @Test
    @DisplayName("Can be targeted by a nonblack spell")
    void canBeTargetedByNonblackSpell() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        SearingWind spell = new SearingWind();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.castInstant(player2, 0, informer.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("Puts a controlled Mercenary in its owner's library")
    void putsControlledMercenaryInOwnersLibrary() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        AgentOfShauku ownedByPlayer1 = new AgentOfShauku();
        ownedByPlayer1.setOwnerId(player1.getId());
        Permanent mercenary = addCreatureReady(player2, ownedByPlayer1);
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mercenary);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).last().isSameAs(mercenary.getCard());
    }
}
