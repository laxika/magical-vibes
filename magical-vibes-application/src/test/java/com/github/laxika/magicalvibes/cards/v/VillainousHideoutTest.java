package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MODOK;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousHideout.class, MODOK.class, GrizzlyBears.class, Mountain.class})
class VillainousHideoutTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent hideout = harness.addToBattlefieldAndReturn(player1, new VillainousHideout());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hideout.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds mana restricted to Villain spells and abilities")
    void addsVillainRestrictedMana() {
        harness.addToBattlefieldAndReturn(player1, new VillainousHideout());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.VILLAIN), ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Villain-restricted mana can pay for a Villain ability")
    void villainRestrictedManaCanPayVillainAbility() {
        harness.addToBattlefield(player1, new VillainousHideout());
        Card villain = createCreatureWithAbility("Test Villain", CardSubtype.VILLAIN);
        harness.addToBattlefield(player1, villain);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaTotal(Set.of(CardSubtype.VILLAIN))).isZero();
    }

    @Test
    @DisplayName("Villainous Hideout makes a target Villain connive")
    void targetVillainConnives() {
        harness.addToBattlefield(player1, new VillainousHideout());
        Permanent villain = addCreatureReady(player1, new MODOK());
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, villain.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The connive ability cannot target a non-Villain or an opponent's creature")
    void conniveAbilityRejectsIllegalTargets() {
        harness.addToBattlefield(player1, new VillainousHideout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MODOK());
        Permanent opponentVillain = findPermanent(player2, "M.O.D.O.K.");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Villain you control");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, opponentVillain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Villain you control");
    }

    @Test
    @DisplayName("The connive ability is restricted to sorcery speed")
    void conniveAbilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new VillainousHideout());
        Permanent villain = addCreatureReady(player1, new MODOK());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, villain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    private static Card createCreatureWithAbility(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setManaCost("{G}");
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.addActivatedAbility(new ActivatedAbility(
                false, "{G}", List.of(new GainLifeEffect(1)), "{G}: You gain 1 life."));
        return card;
    }
}
