package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CosmotronicWave;
import com.github.laxika.magicalvibes.cards.d.DisdainfulStroke;
import com.github.laxika.magicalvibes.cards.i.InescapableBlaze;
import com.github.laxika.magicalvibes.cards.o.OrneryGoblin;
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

@CardUsed({MurmuringMystic.class, InescapableBlaze.class, CosmotronicWave.class,
        OrneryGoblin.class, DisdainfulStroke.class})
class MurmuringMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 1/1 blue Bird Illusion token with flying")
    void instantCreatesBirdIllusion() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new InescapableBlaze()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Bird Illusion");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BIRD, CardSubtype.ILLUSION);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a sorcery creates a Bird Illusion token")
    void sorceryCreatesBirdIllusion() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new CosmotronicWave()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Bird Illusion token")
    void creatureSpellCreatesNoBirdIllusion() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new OrneryGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
    }

    @Test
    @DisplayName("The Bird Illusion is created before the triggering spell resolves")
    void tokenIsCreatedBeforeSpellResolves() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new InescapableBlaze()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bird Illusion")).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Murmuring Mystic")
    void opponentsInstantCreatesNoToken() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player2, List.of(new InescapableBlaze()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        assertThat(countPermanents(player2, "Bird Illusion")).isZero();
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("An opponent's sorcery does not trigger Murmuring Mystic")
    void opponentsSorceryCreatesNoToken() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player2, List.of(new CosmotronicWave()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        assertThat(countPermanents(player2, "Bird Illusion")).isZero();
        harness.assertInGraveyard(player2, "Cosmotronic Wave");
    }

    @Test
    @DisplayName("Each Murmuring Mystic creates a token for the same spell")
    void eachMysticTriggersIndependently() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new InescapableBlaze()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(2);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Each qualifying cast in the same turn creates another token")
    void multipleCastsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new InescapableBlaze(), new InescapableBlaze()));
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(2);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Only the casting player's Mystic creates a token under that player's control")
    void onlyCastersMysticCreatesToken() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.addToBattlefield(player2, new MurmuringMystic());
        harness.setHand(player2, List.of(new InescapableBlaze()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        assertThat(countPermanents(player2, "Bird Illusion")).isEqualTo(1);
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("The token trigger resolves even if Murmuring Mystic dies in response")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        harness.setHand(player1, List.of(new InescapableBlaze()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player2, List.of(new InescapableBlaze()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Murmuring Mystic"));

        harness.assertInGraveyard(player1, "Murmuring Mystic");
        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Countering the triggering spell does not prevent token creation")
    void counteredSpellStillCreatesToken() {
        harness.addToBattlefield(player1, new MurmuringMystic());
        CosmotronicWave spell = new CosmotronicWave();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Cosmotronic Wave");
        assertThat(countPermanents(player1, "Bird Illusion")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird Illusion")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bird Illusion")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
