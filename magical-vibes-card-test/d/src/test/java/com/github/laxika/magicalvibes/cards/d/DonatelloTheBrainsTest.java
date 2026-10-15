package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.cards.r.RootbornDefenses;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloTheBrains.class, MyrBattlesphere.class, WilyGoblin.class, RootbornDefenses.class})
class DonatelloTheBrainsTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one Mutagen token to a token creation event")
    void addsMutagenToTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player1);

        assertThat(findPermanents(player1, "Myr")).hasSize(4);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Adds a Mutagen to a noncreature token creation event")
    void addsMutagenToNoncreatureTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Does not affect an opponent's token creation")
    void doesNotAffectOpponentTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player2);

        assertThat(findPermanents(player2, "Myr")).hasSize(4);
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    @Test
    @DisplayName("Populate adds a Mutagen alongside the copied creature token")
    void addsMutagenWhenPopulating() {
        castMyrBattlesphere(player1);
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        var chosenToken = findPermanents(player1, "Myr").getFirst();

        harness.castFromHand(player1, new RootbornDefenses(), "{2}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenToken.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Myr")).hasSize(5);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Populate with no creature tokens does not create a Mutagen")
    void noMutagenWhenPopulateCreatesNothing() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());

        harness.castFromHand(player1, new RootbornDefenses(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    @DisplayName("Each separate creation event adds another Mutagen")
    void addsMutagenForEachSeparateEvent() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player1);
        castMyrBattlesphere(player1);

        assertThat(findPermanents(player1, "Myr")).hasSize(8);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(2);
    }

    @Test
    @DisplayName("The added Mutagen can be sacrificed to put a counter on a creature")
    void mutagenActivationPutsCounterOnCreature() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player1);
        var mutagen = findPermanents(player1, "Mutagen").getFirst();
        var target = findPermanents(player1, "Myr").getFirst();
        int mutagenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mutagen);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, mutagenIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The generated Mutagen has the predefined Mutagen artifact subtype")
    void generatedMutagenHasMutagenSubtype() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player1);

        var mutagen = findPermanents(player1, "Mutagen").getFirst();
        assertThat(mutagen.getCard().getSubtypes())
                .extracting(subtype -> subtype.name())
                .contains("MUTAGEN");
    }

    private void castMyrBattlesphere(Player player) {
        harness.forceActivePlayer(player);
        harness.castFromHand(player, new MyrBattlesphere(), "{7}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
