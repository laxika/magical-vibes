package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheTrue;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolitaryCamel.class, DesertOfTheTrue.class, Plains.class})
class SolitaryCamelTest extends BaseCardTest {



    @Test
    @DisplayName("Solitary Camel has lifelink while you control a Desert")
    void hasLifelinkWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.addToBattlefield(player1, new DesertOfTheTrue());

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Solitary Camel has lifelink while a Desert card is in your graveyard")
    void hasLifelinkWithDesertInGraveyard() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.setGraveyard(player1, List.of(new DesertOfTheTrue()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Solitary Camel has no lifelink without any Desert")
    void noLifelinkWithoutDesert() {
        harness.addToBattlefield(player1, new SolitaryCamel());

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Desert controlled by the opponent does not grant lifelink")
    void opponentDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.addToBattlefield(player2, new DesertOfTheTrue());

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A Desert card in the opponent's graveyard does not grant lifelink")
    void opponentGraveyardDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.setGraveyard(player2, List.of(new DesertOfTheTrue()));

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Solitary Camel loses lifelink when its only Desert leaves the battlefield")
    void losesLifelinkWhenDesertLeaves() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.addToBattlefield(player1, new DesertOfTheTrue());

        Permanent camel = findPermanent(player1, "Solitary Camel");
        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Desert of the True"));

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A non-Desert land does not grant lifelink")
    void nonDesertDoesNotCount() {
        harness.addToBattlefield(player1, new SolitaryCamel());
        harness.addToBattlefield(player1, new Plains());

        Permanent camel = findPermanent(player1, "Solitary Camel");

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void gainsLifeFromCombatWithDesertOnBattlefield() {
        addCreatureReady(player1, new SolitaryCamel());
        harness.addToBattlefield(player1, new DesertOfTheTrue());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void gainsLifeFromCombatWithDesertOnlyInGraveyard() {
        addCreatureReady(player1, new SolitaryCamel());
        harness.setGraveyard(player1, List.of(new DesertOfTheTrue()));

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void combatWithoutDesertDoesNotGainLife() {
        addCreatureReady(player1, new SolitaryCamel());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void retainsLifelinkWhenDesertMovesToGraveyardThenLosesItWhenRemoved() {
        Permanent camel = harness.addToBattlefieldAndReturn(player1, new SolitaryCamel());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheTrue());
        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setGraveyard(player1, List.of(desert.getCard()));
        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isTrue();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void nonDesertCardInGraveyardDoesNotGrantLifelink() {
        Permanent camel = harness.addToBattlefieldAndReturn(player1, new SolitaryCamel());
        harness.setGraveyard(player1, List.of(new Plains()));

        assertThat(gqs.hasKeyword(gd, camel, Keyword.LIFELINK)).isFalse();
    }
}
