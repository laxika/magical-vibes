package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LeadenMyr;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MyrTurbineTest extends BaseCardTest {

    // ===== First ability: {T}: Create a 1/1 colorless Myr artifact creature token =====

    @Test
    @DisplayName("First ability creates a 1/1 colorless Myr artifact creature token")
    void firstAbilityCreatesMyrToken() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .toList();
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MYR);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("First ability taps the Turbine")
    void firstAbilityTapsTurbine() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(turbine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate first ability when already tapped")
    void cannotActivateFirstAbilityWhenTapped() {
        harness.addToBattlefield(player1, new MyrTurbine());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);
        turbine.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Second ability: {T}, Tap five untapped Myr you control: Search library =====

    @Test
    @DisplayName("Cannot activate second ability without five untapped Myr")
    void cannotActivateSecondAbilityWithoutFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());

        Permanent turbine = findPermanent(player1, "Myr Turbine");
        turbine.setSummoningSick(false);
        // Only 4 Myr — need 5
        setAllNotSummoningSick(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    @DisplayName("Second ability auto-taps when exactly five Myr available")
    void secondAbilityAutoTapsExactlyFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        harness.activateAbility(player1, 0, 1, null, null);

        // All 5 Myr should be tapped
        assertThat(findPermanent(player1, "Gold Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Iron Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Silver Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Leaden Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Copper Myr").isTapped()).isTrue();

        // Turbine should also be tapped (from {T} cost)
        assertThat(findPermanent(player1, "Myr Turbine").isTapped()).isTrue();

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Second ability prompts for choice when more than five Myr available")
    void secondAbilityPromptsWhenMoreThanFiveMyr() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addToBattlefield(player1, new MyrSire());
        setAllNotSummoningSick(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        // Should prompt for choice since 6 Myr > 5 required
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Completing five tap choices puts ability on stack")
    void completingFiveTapChoicesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        harness.addToBattlefield(player1, new MyrSire());
        setAllNotSummoningSick(player1);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GoldMyr());

        UUID goldMyrId = findPermanent(player1, "Gold Myr").getId();
        UUID ironMyrId = findPermanent(player1, "Iron Myr").getId();
        UUID silverMyrId = findPermanent(player1, "Silver Myr").getId();
        UUID leadenMyrId = findPermanent(player1, "Leaden Myr").getId();
        UUID copperMyrId = findPermanent(player1, "Copper Myr").getId();

        harness.activateAbility(player1, 0, 1, null, null);

        // Choose 5 Myr one at a time
        harness.handlePermanentChosen(player1, goldMyrId);
        harness.handlePermanentChosen(player1, ironMyrId);
        harness.handlePermanentChosen(player1, silverMyrId);
        harness.handlePermanentChosen(player1, leadenMyrId);
        harness.handlePermanentChosen(player1, copperMyrId);

        // Ability should now be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Chosen Myr should be tapped
        assertThat(findPermanent(player1, "Gold Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Iron Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Silver Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Leaden Myr").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Copper Myr").isTapped()).isTrue();

        // Unchosen Myr Sire should remain untapped
        assertThat(findPermanent(player1, "Myr Sire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving second ability searches library for Myr creature")
    void resolvingSecondAbilitySearchesForMyrCreature() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Seed library with a Myr creature and a non-Myr
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new MyrSire(), new LlanowarElves()));

        // Exactly 5 Myr -> auto-tap
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should prompt for library search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Only Myr creature cards should be available
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.MYR));

        // Choose Myr Sire
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Myr Sire should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Myr Sire"));
    }

    @Test
    @DisplayName("Non-Myr creatures are excluded from library search")
    void nonMyrCreaturesExcludedFromSearch() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Library has only non-Myr creatures
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // No matching cards — library should be shuffled and search should end
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("Tapped Myr do not count toward the five required")
    void tappedMyrDoNotCount() {
        harness.addToBattlefield(player1, new MyrTurbine());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new LeadenMyr());
        harness.addToBattlefield(player1, new CopperMyr());
        setAllNotSummoningSick(player1);

        // Tap one Myr — only 4 untapped remain
        findPermanent(player1, "Gold Myr").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }

    private void setAllNotSummoningSick(Player player) {
        gd.playerBattlefields.get(player.getId()).forEach(p -> p.setSummoningSick(false));
    }
}
