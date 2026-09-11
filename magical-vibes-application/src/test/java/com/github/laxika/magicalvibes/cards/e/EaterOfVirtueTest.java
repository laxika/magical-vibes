package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KorFirewalker;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EaterOfVirtue.class, GrizzlyBears.class, SerraAngel.class, KorFirewalker.class,
        DoomBlade.class})
class EaterOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eater = addEaterReady(player1);
        eater.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature death exiles the card with Eater of Virtue")
    void equippedCreatureDeathExilesCardWithEater() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eater = addEaterReady(player1);
        eater.setAttachedTo(creature.getId());

        killCreature(creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.getCardsExiledByPermanent(eater.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getCard().getId());
        assertThat(eater.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipped creature gains keywords from cards exiled with Eater of Virtue")
    void equippedCreatureGainsKeywordsFromExiledCards() {
        Permanent firstCreature = addCreatureReady(player1, new SerraAngel());
        Permanent eater = addEaterReady(player1);
        eater.setAttachedTo(firstCreature.getId());

        killCreature(firstCreature);

        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        eater.setAttachedTo(secondCreature.getId());

        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gains fixed protection from exiled cards")
    void equippedCreatureGainsProtectionFromExiledCards() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eater = addEaterReady(player1);
        gd.addToExile(player1.getId(), new KorFirewalker(), eater.getId());
        eater.setAttachedTo(creature.getId());

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
    }

    private Permanent addEaterReady(Player player) {
        Permanent eater = new Permanent(new EaterOfVirtue());
        eater.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(eater);
        return eater;
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
