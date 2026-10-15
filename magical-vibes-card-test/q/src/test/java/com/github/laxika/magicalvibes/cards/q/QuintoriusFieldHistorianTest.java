package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuintoriusFieldHistorian.class, Recollect.class, Reminisce.class, Shock.class})
class QuintoriusFieldHistorianTest extends BaseCardTest {

    @Test
    void boostsOwnSpiritsButNotOtherCreaturesOrOpponentsSpirits() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        harness.addToBattlefield(player1, creatureToken("Own Spirit", 1, 1, CardSubtype.SPIRIT));
        harness.addToBattlefield(player1, creatureToken("Own Bear", 1, 1, CardSubtype.BEAR));
        harness.addToBattlefield(player2, creatureToken("Opponent Spirit", 1, 1, CardSubtype.SPIRIT));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Own Spirit"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Own Bear"))).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Opponent Spirit"))).isEqualTo(1);
    }

    @Test
    void createsRedWhiteSpiritWhenAnyCardLeavesOwnGraveyard() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, shock.getId());
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
    }

    @Test
    void createsOnlyOneSpiritWhenSeveralCardsLeaveTogether() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock(), new Shock())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnOpponentsCardLeavesTheirGraveyard() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player2, List.of(new Recollect()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnEmptyGraveyardIsShuffled() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachSeparateDepartureInTheSameTurn() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Recollect(), new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void pendingTriggerStillCreatesAnUnboostedSpiritAfterQuintoriusDies() {
        harness.addToBattlefield(player1, new QuintoriusFieldHistorian());
        Shock returnedCard = new Shock();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new Recollect()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 2);
        Permanent quintorius = findPermanent(player1, "Quintorius, Field Historian");

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());
        harness.castAndResolveInstant(player2, 0, quintorius.getId());
        harness.castAndResolveInstant(player2, 0, quintorius.getId());
        harness.assertNotOnBattlefield(player1, "Quintorius, Field Historian");
        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, spirits.getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirits.getFirst())).isEqualTo(2);
    }

    private Card creatureToken(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
