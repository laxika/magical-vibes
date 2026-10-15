package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ShamblingGoblin;
import com.github.laxika.magicalvibes.cards.d.Duress;
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

@CardUsed({Corpseweft.class, ShamblingGoblin.class, Duress.class})
class CorpseweftTest extends BaseCardTest {

    @Test
    void exilesCreatureCardsAndCreatesTappedZombieHorrorTwiceTheirNumber() {
        harness.addToBattlefield(player1, new Corpseweft());
        ShamblingGoblin first = new ShamblingGoblin();
        ShamblingGoblin second = new ShamblingGoblin();
        Duress noncreature = new Duress();
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

    @Test
    void rejectsEmptyNoncreatureOpponentAndDuplicateSelections() {
        harness.addToBattlefield(player1, new Corpseweft());
        ShamblingGoblin creature = new ShamblingGoblin();
        ShamblingGoblin opponentCreature = new ShamblingGoblin();
        Duress noncreature = new Duress();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        addMana();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(countPermanents(player1, "Zombie Horror")).isZero();
    }

    @Test
    void separateActivationsKeepTheirOwnExiledCardCounts() {
        harness.addToBattlefield(player1, new Corpseweft());
        ShamblingGoblin first = new ShamblingGoblin();
        ShamblingGoblin second = new ShamblingGoblin();
        ShamblingGoblin third = new ShamblingGoblin();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        assertThat(countPermanents(player1, "Zombie Horror")).isZero();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), third.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Zombie Horror"))
                .toList();
        assertThat(tokens).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(tokens).extracting(token -> gqs.getEffectivePower(gd, token))
                .containsExactlyInAnyOrder(2, 4);
        assertThat(tokens).extracting(token -> gqs.getEffectiveToughness(gd, token))
                .containsExactlyInAnyOrder(2, 4);
        assertThat(countPermanents(player2, "Zombie Horror")).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
