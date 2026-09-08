package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EsikasChariotTest extends BaseCardTest {

    @Test
    void entersWithTwoCatTokens() {
        harness.setHand(player1, List.of(new EsikasChariot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allMatch(cat -> cat.getCard().isToken());
        assertThat(cats).allMatch(cat -> cat.getEffectivePower() == 2 && cat.getEffectiveToughness() == 2);
    }

    @Test
    void attackCreatesCopyOfTargetTokenYouControl() {
        Permanent chariot = addChariotReady();
        Permanent crew = addCreatureReady(player1, new SerraAngel());
        Permanent targetToken = addTokenCreature("Cat", 2, 2, CardColor.GREEN, CardSubtype.CAT);
        targetToken.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, targetToken.getId());
        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).filteredOn(cat -> cat.getCard().isToken()).hasSize(2);
        assertThat(chariot.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackCannotTargetNonTokenPermanent() {
        addChariotReady();
        addCreatureReady(player1, new SerraAngel());
        Permanent targetToken = addTokenCreature("Cat", 2, 2, CardColor.GREEN, CardSubtype.CAT);
        targetToken.tap();
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());
        nonToken.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChariotReady() {
        Permanent chariot = addCreatureReady(player1, new EsikasChariot());
        chariot.setSummoningSick(false);
        return chariot;
    }

    private Permanent addTokenCreature(String name, int power, int toughness, CardColor color,
                                       CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        return addCreatureReady(player1, card);
    }
}
