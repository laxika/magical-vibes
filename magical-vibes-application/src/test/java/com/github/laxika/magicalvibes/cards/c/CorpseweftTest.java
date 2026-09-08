package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Corpseweft.class, GrizzlyBears.class, Shock.class})
class CorpseweftTest extends BaseCardTest {

    @Test
    void exilesCreatureCardsAndCreatesTappedZombieHorrorTwiceTheirNumber() {
        harness.addToBattlefield(player1, new Corpseweft());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock noncreature = new Shock();
        harness.setGraveyard(player1, List.of(first, second, noncreature));
        addMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);

        Permanent token = findPermanent(player1, "Zombie Horror");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.HORROR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    void mustExileAtLeastOneCreatureCard() {
        harness.addToBattlefield(player1, new Corpseweft());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(countPermanents(player1, "Zombie Horror")).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
