package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.z.ZombieOutlander;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathMaskDuplicantTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature from the controller's graveyard and gains its keywords")
    void exilesOwnCreatureAndGainsKeywords() {
        Permanent duplicant = addDuplicantReady(player1);
        Card angel = new SerraAngel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(angel)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Serra Angel");
        assertThat(gd.getCardsExiledByPermanent(duplicant.getId())).containsExactly(angel);
        assertThat(gqs.computeStaticBonus(gd, duplicant).keywords())
                .contains(Keyword.FLYING)
                .doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addDuplicantReady(player1);
        Card angel = new SerraAngel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(angel)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, angel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combines keywords and fixed protection from every creature card exiled with it")
    void combinesAbilitiesFromAllExiledCreatures() {
        Permanent duplicant = addDuplicantReady(player1);
        gd.addToExile(player1.getId(), new SerraAngel(), duplicant.getId());
        gd.addToExile(player1.getId(), new ZombieOutlander(), duplicant.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears());

        assertThat(gqs.computeStaticBonus(gd, duplicant).keywords())
                .contains(Keyword.FLYING)
                .doesNotContain(Keyword.VIGILANCE)
                .doesNotContain(Keyword.DEFENDER);
        assertThat(gqs.hasProtectionFrom(gd, duplicant, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, duplicant, CardColor.RED)).isFalse();
    }

    private Permanent addDuplicantReady(Player player) {
        Permanent perm = new Permanent(new DeathMaskDuplicant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
