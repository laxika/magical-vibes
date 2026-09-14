package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.cards.g.GoblinVandal;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SouthernPaladin;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudDjinn.class, DenseFoliage.class, GoblinVandal.class, Opalescence.class, Shock.class, SouthernPaladin.class, Thunderbolt.class})
class DenseFoliageTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cannot target a creature while Dense Foliage is out")
    void spellsCannotTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());
        harness.addToBattlefield(player2, new CloudDjinn());

        prepareThunderbolt();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(harness.getPermanentId(player2, "Cloud Djinn"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Spells cannot target your own creature while Dense Foliage is out")
    void spellsCannotTargetOwnCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());
        harness.addToBattlefield(player1, new CloudDjinn());

        prepareThunderbolt();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(harness.getPermanentId(player1, "Cloud Djinn"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Spells can still target players while Dense Foliage is out")
    void spellsCanStillTargetPlayers() {
        harness.addToBattlefield(player1, new DenseFoliage());

        prepareThunderbolt();

        harness.castModalInstant(player1, 0, 0, List.of(player2.getId()));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Thunderbolt"));
    }

    @Test
    @DisplayName("Without Dense Foliage a spell can target the creature")
    void spellsTargetCreaturesWithoutDenseFoliage() {
        harness.addToBattlefield(player2, new CloudDjinn());

        prepareThunderbolt();

        harness.castModalInstant(player1, 0, 1,
                List.of(harness.getPermanentId(player2, "Cloud Djinn")));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Thunderbolt"));
    }

    @Test
    @DisplayName("Activated abilities can still target creatures while Dense Foliage is out")
    void abilitiesCanStillTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());

        Permanent target = addCreatureReady(player1, new GoblinVandal());
        addCreatureReady(player2, new SouthernPaladin());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Vandal");
    }

    private void prepareThunderbolt() {
        harness.setHand(player1, List.of(new Thunderbolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Dense Foliage still protects itself when it becomes a creature")
    void creatureVersionCannotBeTargetedBySpells() {
        Permanent denseFoliage = harness.addToBattlefieldAndReturn(player1, new DenseFoliage());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, denseFoliage)).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, denseFoliage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }
}
