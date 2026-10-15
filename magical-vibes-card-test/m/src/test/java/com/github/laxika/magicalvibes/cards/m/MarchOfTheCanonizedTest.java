package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodArtist;
import com.github.laxika.magicalvibes.cards.c.CordialVampire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfTheCanonized.class, BloodArtist.class, CordialVampire.class})
class MarchOfTheCanonizedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X white lifelink Vampire tokens when it enters")
    void etbCreatesXWhiteLifelinkVampires() {
        harness.setHand(player1, List.of(new MarchOfTheCanonized()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates a flying Vampire Demon when combined white and black devotion reaches seven")
    void upkeepCreatesVampireDemonAtThreshold() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new BloodArtist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Vampire Demon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Vampire Demon below seven combined devotion")
    void upkeepDoesNotCreateVampireDemonBelowThreshold() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new BloodArtist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }

    @Test
    void zeroXCreatesNoVampires() {
        harness.setHand(player1, List.of(new MarchOfTheCanonized()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
        assertThat(findPermanents(player1, "March of the Canonized")).hasSize(1);
    }

    @Test
    void devotionMustStillBeSevenWhenTriggerResolves() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new CordialVampire());
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new BloodArtist());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(artist);
        harness.setHand(player1, List.of(artist.getCard()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }

    @Test
    void gainingDevotionAfterUpkeepBeginsDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new CordialVampire());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new BloodArtist());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }

    @Test
    void opponentPermanentsDoNotCountTowardDevotion() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player2, new CordialVampire());
        harness.addToBattlefield(player2, new CordialVampire());
        harness.addToBattlefield(player2, new BloodArtist());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MarchOfTheCanonized());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new CordialVampire());
        harness.addToBattlefield(player1, new BloodArtist());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Vampire Demon")).isEmpty();
    }
}
