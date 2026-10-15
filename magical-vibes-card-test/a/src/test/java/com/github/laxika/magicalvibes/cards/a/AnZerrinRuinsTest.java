package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.e.EbonyRhino;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnZerrinRuins.class, DwarvenTrader.class, EbonyRhino.class})
class AnZerrinRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature of the chosen type stays tapped through its controller's untap step")
    void chosenTypeDoesNotUntap() {
        addRuins(player1, CardSubtype.DWARF);
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());
        dwarf.tap();

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature of another type untaps normally")
    void otherTypeUntaps() {
        addRuins(player1, CardSubtype.DWARF);
        Permanent rhino = addCreatureReady(player1, new EbonyRhino());
        rhino.tap();

        advanceToUpkeep(player1);

        assertThat(rhino.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locks opponents' creatures of the chosen type too")
    void affectsOpponentCreatures() {
        addRuins(player1, CardSubtype.DWARF);
        Permanent opponentDwarf = addCreatureReady(player2, new DwarvenTrader());
        opponentDwarf.tap();

        advanceToUpkeep(player2);

        assertThat(opponentDwarf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The lock remains active while the Ruins is tapped")
    void lockAppliesWhileRuinsIsTapped() {
        Permanent ruins = addRuins(player1, CardSubtype.DWARF);
        ruins.tap();
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());
        dwarf.tap();

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once the Ruins leaves the battlefield, the lock is gone")
    void untapsAfterRuinsLeaves() {
        Permanent ruins = addRuins(player1, CardSubtype.DWARF);
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());
        dwarf.tap();

        gd.playerBattlefields.get(player1.getId()).remove(ruins);

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting the Ruins prompts for a creature type, which then locks matching creatures")
    void choosesTypeOnEnter() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());
        dwarf.tap();

        harness.setHand(player1, List.of(new AnZerrinRuins()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DWARF");

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Separate Ruins retain independent choices, and removing one releases only its type")
    void separateRuinsHaveIndependentChoices() {
        Permanent dwarfRuins = addRuins(player1, CardSubtype.DWARF);
        addRuins(player2, CardSubtype.RHINO);
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());
        Permanent rhino = addCreatureReady(player1, new EbonyRhino());
        dwarf.tap();
        rhino.tap();

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isTrue();
        assertThat(rhino.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dwarfRuins);
        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isFalse();
        assertThat(rhino.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Matching creatures that are untapped stay untapped")
    void doesNotTapMatchingCreatures() {
        addRuins(player1, CardSubtype.DWARF);
        Permanent dwarf = addCreatureReady(player1, new DwarvenTrader());

        advanceToUpkeep(player1);

        assertThat(dwarf.isTapped()).isFalse();
    }

    private Permanent addRuins(Player player, CardSubtype chosen) {
        Permanent ruins = harness.addToBattlefieldAndReturn(player, new AnZerrinRuins());
        ruins.setChosenSubtype(chosen);
        return ruins;
    }

}
