package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CagedSunTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Caged Sun has correct effects")
    void hasCorrectEffects() {
        CagedSun card = new CagedSun();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseColorOnEnterEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostCreaturesOfChosenColorEffect.class);
        BoostCreaturesOfChosenColorEffect boost = (BoostCreaturesOfChosenColorEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND).getFirst())
                .isInstanceOf(AddExtraManaOfChosenColorOnLandTapEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Caged Sun puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Caged Sun");
    }

    @Test
    @DisplayName("Resolving Caged Sun enters battlefield and awaits color choice")
    void resolvingTriggersColorChoice() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Caged Sun"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on Caged Sun")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        Permanent cagedSun = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Caged Sun"))
                .findFirst().orElseThrow();
        assertThat(cagedSun.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    // ===== Static effect: +1/+1 to creatures of chosen color =====

    @Test
    @DisplayName("Creatures of chosen color get +1/+1")
    void boostsCreaturesOfChosenColor() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);

        // Add Caged Sun with chosen color green
        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Green Bear"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures of different color do not get boosted")
    void doesNotBoostDifferentColor() {
        Card redCreature = createCreature("Red Goblin", 1, 1, CardColor.RED);
        harness.addToBattlefield(player1, redCreature);

        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Red Goblin"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures of chosen color")
    void doesNotBoostOpponentCreatures() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player2, greenCreature);

        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        Permanent bear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Green Bear"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost if no color was chosen yet")
    void noBoostWithoutChosenColor() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);

        // Add Caged Sun without setting chosen color
        harness.addToBattlefield(player1, new CagedSun());

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Green Bear"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    // ===== Mana trigger: extra mana on land tap =====

    @Test
    @DisplayName("Tapping a Forest with green chosen adds extra green mana")
    void extraManaOnMatchingLandTap() {
        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        harness.addToBattlefield(player1, new Forest());

        // Forest is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get 2 green mana: 1 from Forest + 1 from Caged Sun trigger
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a land of non-chosen color does not add extra mana")
    void noExtraManaOnNonMatchingLandTap() {
        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        harness.addToBattlefield(player1, new Mountain());

        // Mountain is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get 1 red mana (no bonus)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's land tap does not trigger extra mana")
    void noExtraManaForOpponentLandTap() {
        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        harness.addToBattlefield(player2, new Forest());

        // Opponent taps their Forest
        harness.tapPermanent(player2, 0);

        // Opponent should get 1 green mana only (no bonus from player1's Caged Sun)
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Forests each get the extra mana")
    void extraManaOnMultipleLandTaps() {
        CagedSun cagedSunCard = new CagedSun();
        Permanent cagedSunPerm = new Permanent(cagedSunCard);
        cagedSunPerm.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(cagedSunPerm);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        // Tap both Forests (indices 1 and 2, Caged Sun at 0)
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);

        // Should get 4 green mana: (1+1) + (1+1)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("No extra mana if no color was chosen yet")
    void noExtraManaWithoutChosenColor() {
        // Add Caged Sun without chosen color
        harness.addToBattlefield(player1, new CagedSun());
        harness.addToBattlefield(player1, new Forest());

        // Forest is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get only 1 green mana (no bonus)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    // ===== Full cast-and-resolve integration =====

    @Test
    @DisplayName("Full flow: cast, resolve, choose color, boost creature, get extra mana")
    void fullIntegrationTest() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Cast and resolve Caged Sun
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Choose green
        harness.handleListChoice(player1, "GREEN");

        // Verify creature is boosted
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Green Bear"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        // Find Forest index (it's before Caged Sun since it was added first)
        int forestIndex = -1;
        var bf = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Forest")) {
                forestIndex = i;
                break;
            }
        }
        assertThat(forestIndex).isGreaterThanOrEqualTo(0);

        // Tap Forest for extra mana
        harness.tapPermanent(player1, forestIndex);

        // Should get 2 green mana: 1 from Forest + 1 from Caged Sun
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
