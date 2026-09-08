package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PetrifiedHamletTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a name offers only land card names")
    void choosesOnlyLandNames() {
        harness.setHand(player1, List.of(new PetrifiedHamlet(), new Forest(), new GrizzlyBears()));

        harness.playLand(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Forest");
        assertThat(choice.options()).doesNotContain("Grizzly Bears");
        assertThat(choice.prompt()).isEqualTo("Choose a land card name.");
        harness.assertNotOnBattlefield(player1, "Petrified Hamlet");
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
        PetrifiedHamlet card = new PetrifiedHamlet();
        card.setName("Petrified Hamlet");
        card.setType(CardType.LAND);
        Permanent permanent = new Permanent(card);
        permanent.setChosenName("Petrified Hamlet");
        gd.playerBattlefields.get(player1.getId()).add(permanent);

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

    private Permanent addReadyHamlet(com.github.laxika.magicalvibes.model.Player player, String chosenName) {
        PetrifiedHamlet card = new PetrifiedHamlet();
        Permanent permanent = new Permanent(card);
        permanent.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
