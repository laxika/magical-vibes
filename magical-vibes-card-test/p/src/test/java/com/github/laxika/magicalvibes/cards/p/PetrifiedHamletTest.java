package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.d.DrownyardTemple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AberrantManawurm;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PetrifiedHamlet.class, Forest.class, Island.class, AberrantManawurm.class, TerramorphicExpanse.class})
class PetrifiedHamletTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a name offers only land card names")
    void choosesOnlyLandNames() {
        harness.setHand(player1, List.of(new PetrifiedHamlet(), new Forest(), new AberrantManawurm()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Petrified Hamlet");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Forest");
        assertThat(choice.options()).doesNotContain("Aberrant Manawurm");
        assertThat(choice.prompt()).isEqualTo("Choose a land card name.");
        harness.assertOnBattlefield(player1, "Petrified Hamlet");
        harness.handleListChoice(player1, "Forest");
        assertThat(findPermanent(player1, "Petrified Hamlet").getChosenName()).isEqualTo("Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Matching lands gain the colorless mana ability on both battlefields")
    void matchingLandsGainColorlessManaAbility() {
        addReadyHamlet(player1, "Forest");
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The chosen name can grant the ability to Petrified Hamlet itself")
    void matchingNameGrantsAbilityToSource() {
        addReadyHamlet(player1, "Petrified Hamlet");

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Differently named lands keep their normal mana ability")
    void differentlyNamedLandsAreNotGrantedColorlessMana() {
        addReadyHamlet(player1, "Forest");
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The chosen land's non-mana abilities are blocked")
    void blocksNonManaAbilitiesOfChosenName() {
        addReadyHamlet(player1, "Forest");
        Card landCard = new Card();
        landCard.setName("Forest");
        landCard.setType(CardType.LAND);
        landCard.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Forest deals 1 damage to any target."));
        Permanent land = new Permanent(landCard);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Chosen lands retain their original colored mana ability")
    void chosenLandsKeepOriginalManaAbility() {
        addReadyHamlet(player1, "Forest");
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Hamlet can produce colorless mana without any chosen name")
    void sourceProducesManaBeforeANameIsChosen() {
        harness.addToBattlefield(player1, new PetrifiedHamlet());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A real chosen land cannot activate its sacrifice ability for either player")
    void blocksRealLandNonManaAbilityForBothPlayers() {
        addReadyHamlet(player1, "Terramorphic Expanse");
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addToBattlefield(player2, new TerramorphicExpanse());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        harness.assertOnBattlefield(player1, "Terramorphic Expanse");
        harness.assertOnBattlefield(player2, "Terramorphic Expanse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({DrownyardTemple.class})
    @DisplayName("The chosen name also blocks non-mana abilities from the graveyard")
    void blocksChosenNameInGraveyard() {
        addReadyHamlet(player1, "Drownyard Temple");
        harness.setGraveyard(player2, List.of(new DrownyardTemple()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BloodMoon.class, DrownyardTemple.class})
    @DisplayName("Blood Moon removes Hamlet's name-based activation restriction")
    void bloodMoonRemovesNameLock() {
        addReadyHamlet(player1, "Drownyard Temple");
        harness.addToBattlefield(player1, new BloodMoon());
        harness.setGraveyard(player2, List.of(new DrownyardTemple()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drownyard Temple");
        harness.assertNotInGraveyard(player2, "Drownyard Temple");
        assertThat(findPermanent(player2, "Drownyard Temple").isTapped()).isTrue();
    }

    private Permanent addReadyHamlet(com.github.laxika.magicalvibes.model.Player player, String chosenName) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PetrifiedHamlet());
        permanent.setChosenName(chosenName);
        return permanent;
    }
}
