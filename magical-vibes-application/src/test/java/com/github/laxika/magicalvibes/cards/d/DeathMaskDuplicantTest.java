package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TelJiladOutrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathMaskDuplicant.class, DarksteelGargoyle.class, DarksteelIngot.class,
        TelJiladOutrider.class})
class DeathMaskDuplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature from the controller's graveyard and gains its keywords")
    void exilesOwnCreatureAndGainsKeywords() {
        Permanent duplicant = addDuplicantReady(player1);
        Card flyingCreature = new DarksteelGargoyle();
        harness.setGraveyard(player1, new ArrayList<>(List.of(flyingCreature)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, flyingCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Darksteel Gargoyle");
        assertThat(gd.getCardsExiledByPermanent(duplicant.getId())).containsExactly(flyingCreature);
        assertThat(gqs.computeStaticBonus(gd, duplicant).keywords())
                .contains(Keyword.FLYING)
                .doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addDuplicantReady(player1);
        Card creature = new DarksteelGargoyle();
        harness.setGraveyard(player2, new ArrayList<>(List.of(creature)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the controller's graveyard")
    void cannotTargetNonCreatureCard() {
        addDuplicantReady(player1);
        Card artifact = new DarksteelIngot();
        harness.setGraveyard(player1, new ArrayList<>(List.of(artifact)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Combines keywords and fixed protection from every creature card exiled with it")
    void combinesAbilitiesFromAllExiledCreatures() {
        Permanent duplicant = addDuplicantReady(player1);
        Card flyingCreature = new DarksteelGargoyle();
        Card protectionCreature = new TelJiladOutrider();
        Card nonCreatureArtifact = new DarksteelIngot();
        gd.addToExile(player1.getId(), flyingCreature, duplicant.getId());
        gd.addToExile(player1.getId(), protectionCreature, duplicant.getId());
        gd.addToExile(player1.getId(), nonCreatureArtifact, duplicant.getId());

        assertThat(gqs.computeStaticBonus(gd, duplicant).keywords())
                .contains(Keyword.FLYING)
                .doesNotContain(Keyword.INDESTRUCTIBLE, Keyword.REACH);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, duplicant, nonCreatureArtifact)).isTrue();
    }

    private Permanent addDuplicantReady(Player player) {
        return addCreatureReady(player, new DeathMaskDuplicant());
    }
}
