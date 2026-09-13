package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DryadOfTheIlysianGrove;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Piracy;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VernalBloom.class, Forest.class, Mountain.class, DryadOfTheIlysianGrove.class,
        Twiddle.class, Piracy.class})
class VernalBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Forest for mana adds an additional green mana")
    void forestProducesExtraGreen() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        // 1 from Forest + 1 additional from Vernal Bloom = 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a non-Forest land does not trigger the additional mana")
    void nonForestDoesNotTrigger() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's Forest also produces the additional green")
    void opponentForestAlsoBenefits() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        // The Forest's controller (player2) gets the additional {G}.
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Forest tapped through Piracy gives the bonus to the Forest's controller")
    void foreignForestBonusGoesToForestController() {
        harness.addToBattlefield(player1, new VernalBloom());
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new Piracy(), "{U}{U}");
        harness.passBothPriorities();
        harness.tapForeignLandForMana(player1, forest.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Forest tapped produces its own additional green")
    void multipleForestsEachTrigger() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);

        // 2 Forests * 2 green each = 4
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Vernal Bloom adds its own additional green mana")
    void multipleVernalBloomsEachTrigger() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        // 1 from Forest + 1 additional from each Vernal Bloom = 3.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("A land that gains the Forest subtype also triggers Vernal Bloom")
    void gainedForestSubtypeTriggers() {
        harness.addToBattlefield(player1, new VernalBloom());
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a Forest without producing mana does not trigger Vernal Bloom")
    void nonManaTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new VernalBloom());
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Without Vernal Bloom in play a Forest produces only one green")
    void noExtraWithoutVernalBloom() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
