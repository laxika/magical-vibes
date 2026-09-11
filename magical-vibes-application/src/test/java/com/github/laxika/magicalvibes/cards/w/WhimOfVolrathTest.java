package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlickeringWard;
import com.github.laxika.magicalvibes.cards.s.SoltariPriest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhimOfVolrath.class, SoltariPriest.class, Forest.class, FlickeringWard.class})
class WhimOfVolrathTest extends BaseCardTest {

    @Test
    @DisplayName("Replacing a color word records the change on the target permanent")
    void replacesColorWord() {
        harness.addToBattlefield(player2, new SoltariPriest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Soltari Priest");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        Permanent perm = findPermanent(player2, "Soltari Priest");
        assertThat(perm.getTextReplacements())
                .containsExactly(new TextReplacement("black", "green", true));
        assertThat(graveyardNames(player1)).containsExactly("Whim of Volrath");
    }

    @Test
    @DisplayName("Changing red to green changes Soltari Priest's printed protection")
    void changesPrintedColorWord() {
        Permanent priest = harness.addToBattlefieldAndReturn(player2, new SoltariPriest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, priest.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, priest, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, priest, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Replacing a basic land type records the change on the target permanent")
    void replacesLandType() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        Permanent perm = findPermanent(player2, "Forest");
        assertThat(perm.getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Forest", true));
    }

    @Test
    @DisplayName("Changing a basic land type changes a basic land's effective type and mana")
    void changesBasicLandTypeAndMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleListChoice(player1, "FOREST");
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The text change wears off at end of turn")
    void textChangeWearsOff() {
        harness.addToBattlefield(player2, new SoltariPriest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Soltari Priest");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        Permanent perm = findPermanent(player2, "Soltari Priest");
        assertThat(perm.getTextReplacements()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(perm.getTextReplacements()).isEmpty();
    }

    @Test
    @DisplayName("A color-word change does not change a color chosen as the permanent entered")
    void textChangeDoesNotChangeChosenColor() {
        Permanent priest = harness.addToBattlefieldAndReturn(player2, new SoltariPriest());
        Permanent flickeringWard = harness.addToBattlefieldAndReturn(player2, new FlickeringWard());
        flickeringWard.setAttachedTo(priest.getId());
        flickeringWard.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Flickering Ward");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        assertThat(flickeringWard.getChosenColor()).isEqualTo(CardColor.BLACK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flickeringWard.getChosenColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Paying buyback returns Whim of Volrath to its owner's hand as it resolves")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");

        assertThat(handNames(player1)).containsExactly("Whim of Volrath");
        assertThat(graveyardNames(player1)).isEmpty();
    }

    @Test
    @DisplayName("A buyback spell that fizzles goes to the graveyard")
    void buybackFizzleGoesToGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WhimOfVolrath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithBuyback(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Whim of Volrath");
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
