package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorophonTheBoundless.class, GrizzlyBears.class, HillGiant.class})
class MorophonTheBoundlessTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Morophon enters stores that type")
    void choosesCreatureTypeOnEntry() {
        harness.setHand(player1, List.of(new MorophonTheBoundless()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Morophon, the Boundless").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Spells of the chosen type lose only their colored mana cost")
    void reducesColoredManaForChosenTypeSpell() {
        addMorophon(player1, CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The chosen-type reduction applies to noncreature spells")
    void reducesNoncreatureSpellOfChosenType() {
        addMorophon(player1, CardSubtype.BEAR);
        Card bearSpell = createNoncreatureBearSpell();
        harness.setHand(player1, List.of(bearSpell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bearSpell);
    }

    @Test
    @DisplayName("Spells without the chosen type are not reduced")
    void doesNotReduceDifferentTypeSpell() {
        addMorophon(player1, CardSubtype.BEAR);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Other creatures you control of the chosen type get +1/+1")
    void boostsOtherOwnCreaturesOfChosenType() {
        Permanent morophon = addMorophon(player1, CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.computeStaticBonus(gd, bear).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, bear).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, opponentBear).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, morophon).power()).isZero();
    }

    @Test
    @DisplayName("The static effects disappear when Morophon leaves the battlefield")
    void effectsDisappearWhenMorophonLeaves() {
        addMorophon(player1, CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.computeStaticBonus(gd, bear).power()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard() instanceof MorophonTheBoundless);

        assertThat(gqs.computeStaticBonus(gd, bear).power()).isZero();
    }

    private Permanent addMorophon(Player player, CardSubtype chosenSubtype) {
        Permanent morophon = new Permanent(new MorophonTheBoundless());
        morophon.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player.getId()).add(morophon);
        return morophon;
    }

    private static Card createNoncreatureBearSpell() {
        Card card = new Card();
        card.setName("Bear Ritual");
        card.setType(CardType.SORCERY);
        card.setManaCost("{2}{W}{U}{B}{R}{G}");
        card.setColor(CardColor.WHITE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}
