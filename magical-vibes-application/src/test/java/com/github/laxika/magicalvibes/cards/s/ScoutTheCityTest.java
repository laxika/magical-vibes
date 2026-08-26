package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ScoutTheCity.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class})
class ScoutTheCityTest extends BaseCardTest {

    @Test
    @DisplayName("Look Around mills three, returns a milled permanent, and gains life")
    void lookAroundReturnsMilledPermanentAndGainsLife() {
        setDeck(new Forest(), new Shock(), new Shock());
        harness.setLife(player1, 20);

        castLookAround();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Look Around gains life when no milled permanent is available")
    void lookAroundGainsLifeWithoutPermanent() {
        setDeck(new Shock(), new Shock(), new Shock());
        harness.setLife(player1, 20);

        castLookAround();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Bring Down destroys a creature with flying")
    void bringDownDestroysCreatureWithFlying() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castBringDown(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Bring Down cannot target a creature without flying")
    void bringDownRejectsCreatureWithoutFlying() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    private void castLookAround() {
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void castBringDown(Permanent target) {
        harness.setHand(player1, List.of(new ScoutTheCity()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
