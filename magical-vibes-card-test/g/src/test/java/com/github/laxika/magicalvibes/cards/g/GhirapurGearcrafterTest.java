package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhirapurGearcrafter.class, FieryImpulse.class})
class GhirapurGearcrafterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.castFromHand(player1, new GhirapurGearcrafter(), "{2}{R}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent thopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElse(null);

        assertThat(thopter).isNotNull();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("The Thopter is created only when the separate ETB trigger resolves")
    void tokenWaitsForTriggerResolution() {
        harness.castFromHand(player1, new GhirapurGearcrafter(), "{2}{R}");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghirapur Gearcrafter");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())
                .stream().filter(p -> p.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates one Thopter for the entering creature's controller")
    void noncastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new GhirapurGearcrafter());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(tokens.getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Gearcrafter in response does not stop its Thopter trigger")
    void triggerResolvesAfterSourceDies() {
        harness.castFromHand(player1, new GhirapurGearcrafter(), "{2}{R}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new FieryImpulse()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Ghirapur Gearcrafter"));

        harness.assertNotOnBattlefield(player1, "Ghirapur Gearcrafter");
        harness.assertInGraveyard(player1, "Ghirapur Gearcrafter");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thopter");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
