package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntedTroll.class, Char.class, Putrefy.class})
class HuntedTrollTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates four blue 1/1 Faerie tokens with flying under the targeted opponent's control")
    void etbCreatesFaerieTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        List<Permanent> faeries = findPermanents(player2, "Faerie");
        assertThat(faeries).hasSize(4);
        assertThat(findPermanents(player1, "Faerie")).isEmpty();

        for (Permanent faerie : faeries) {
            assertThat(faerie.getCard().isToken()).isTrue();
            assertThat(faerie.getCard().getPower()).isEqualTo(1);
            assertThat(faerie.getCard().getToughness()).isEqualTo(1);
            assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(faerie.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(faerie.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE);
            assertThat(gqs.hasKeyword(gd, faerie, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("ETB chooses an opponent when Hunted Troll enters without being cast")
    void etbChoosesOpponentWhenEnteringWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new HuntedTroll());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Faerie")).hasSize(4);
        assertThat(findPermanents(player1, "Faerie")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        harness.addToBattlefield(player1, new HuntedTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Hunted Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration replaces lethal damage destruction and clears marked damage")
    void regenerationSavesTrollFromLethalDamage() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HuntedTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Char()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isFalse();
        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertOnBattlefield(player1, "Hunted Troll");
        harness.assertNotInGraveyard(player1, "Hunted Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield cannot save Hunted Troll from Putrefy")
    void regenerationDoesNotPreventDestructionThatForbidsIt() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HuntedTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertNotOnBattlefield(player1, "Hunted Troll");
        harness.assertInGraveyard(player1, "Hunted Troll");
    }

    @Test
    @DisplayName("The enters trigger creates Faeries even after Hunted Troll leaves the battlefield")
    void entersTriggerResolvesAfterSourceLeaves() {
        Permanent troll = harness.enterBattlefieldAndReturn(player1, new HuntedTroll());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, troll.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hunted Troll");
        assertThat(findPermanents(player2, "Faerie")).hasSize(4);
        assertThat(findPermanents(player1, "Faerie")).isEmpty();
    }
}
