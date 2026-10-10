package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GraveyardMarshal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesecratedTomb.class, Disentomb.class, GrizzlyBears.class, Recollect.class,
        Reminisce.class, Shock.class, GraveyardMarshal.class, RiseFromTheGrave.class})
class DesecratedTombTest extends BaseCardTest {

    @Test
    void createsBatWhenCreatureCardLeavesYourGraveyard() {
        addReadyTomb(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenNoncreatureCardLeavesYourGraveyard() {
        addReadyTomb(player1);
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsOnlyOneBatWhenSeveralCreatureCardsLeaveTogether() {
        addReadyTomb(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenOpponentCreatureCardsLeaveGraveyard() {
        addReadyTomb(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(findPermanents(player2, "Bat")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void triggersForEachSeparateDepartureInTheSameTurn() {
        addReadyTomb(player1);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Recollect(), new Recollect()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bat")).hasSize(1);

        harness.castSorcery(player1, 0, second.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Bat")).hasSize(2);
    }

    @Test
    void eachTombTriggersOnceForASimultaneousMixedDeparture() {
        addReadyTomb(player1);
        addReadyTomb(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(2);
    }

    @Test
    void triggersWhenCreatureReturnsToBattlefield() {
        addReadyTomb(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().getId()).isEqualTo(bears.getId());
    }

    @Test
    void exilingCreatureAsACostCreatesAnUntappedFlyingBlackBat() {
        addReadyTomb(player1);
        harness.addToBattlefield(player1, new GraveyardMarshal());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
        var bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().isToken()).isTrue();
        assertThat(bat.getCard().getPower()).isEqualTo(1);
        assertThat(bat.getCard().getToughness()).isEqualTo(1);
        assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(bat.getCard().getSubtypes()).containsExactly(CardSubtype.BAT);
        assertThat(bat.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bat.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    private void addReadyTomb(Player player) {
        harness.addToBattlefield(player, new DesecratedTomb());
    }
}
