package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.h.HoarShade;
import com.github.laxika.magicalvibes.cards.k.KnightOfStromgald;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Drought.class, FyndhornElves.class, HoarShade.class, KnightOfStromgald.class, Swamp.class})
class DroughtTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W}{W} at upkeep keeps Drought")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new Drought());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drought");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Drought")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new Drought());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drought");
    }

    @Test
    @DisplayName("Casting a black spell requires sacrificing a Swamp")
    void blackSpellRequiresSwampSacrifice() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new HoarShade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithImposedSacrifice(player1, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hoar Shade");
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Casting a spell with two black symbols requires two Swamp sacrifices")
    void blackSpellRequiresOneSwampPerBlackSymbol() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        List<Permanent> swamps = gd.playerBattlefields.get(player1.getId());
        harness.setHand(player1, List.of(new KnightOfStromgald()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithImposedSacrifice(player1, 0,
                List.of(swamps.get(0).getId(), swamps.get(1).getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knight of Stromgald");
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Casting a black spell without a Swamp fails")
    void blackSpellWithoutSwampFails() {
        harness.addToBattlefield(player2, new Drought());
        harness.setHand(player1, List.of(new HoarShade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithImposedSacrifice(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice");
    }

    @Test
    @DisplayName("Non-black spells cast without a Swamp sacrifice")
    void nonBlackSpellUnaffected() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new FyndhornElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fyndhorn Elves");
        harness.assertOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Activating a {B} ability requires sacrificing a Swamp")
    void blackAbilityRequiresSwampSacrifice() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new HoarShade());
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        // Exactly one Swamp → auto-pays the imposed sacrifice
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Activating a {B} ability without a Swamp fails")
    void blackAbilityWithoutSwampFails() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new HoarShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating an ability with two black symbols requires two Swamp sacrifices")
    void blackAbilityRequiresOneSwampPerBlackSymbol() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new KnightOfStromgald());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("An activated ability without black symbols needs no Swamp sacrifice")
    void nonBlackAbilityUnaffected() {
        harness.addToBattlefield(player2, new Drought());
        Permanent elves = addCreatureReady(player1, new FyndhornElves());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elves.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Swamp");
    }
}
