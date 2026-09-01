package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.p.PhantasmalFiend;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.cards.s.SoldeviDigger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        SoldeviAdnate.class,
        PhantasmalFiend.class,
        AesthirGlider.class,
        ElvishRanger.class,
        SoldeviDigger.class,
        ShieldSphere.class
})
class SoldeviAdnateTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a black creature adds {B} equal to its mana value")
    void sacrificeBlackCreatureAddsManaValueInBlack() {
        Permanent adnate = addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new PhantasmalFiend()); // {3}{B}, mana value 4
        UUID fiend = harness.getPermanentId(player1, "Phantasmal Fiend");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fiend);

        harness.assertInGraveyard(player1, "Phantasmal Fiend");
        assertThat(adnate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }

    @Test
    @DisplayName("An artifact creature is a legal sacrifice")
    void sacrificeArtifactCreature() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new AesthirGlider()); // colorless artifact creature, mana value 3
        UUID glider = harness.getPermanentId(player1, "Aesthir Glider");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glider);

        harness.assertInGraveyard(player1, "Aesthir Glider");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Soldevi Adnate can sacrifice itself")
    void sacrificesItself() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new ElvishRanger()); // green nonartifact, ineligible

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Soldevi Adnate");
        harness.assertOnBattlefield(player1, "Elvish Ranger");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that is neither black nor an artifact cannot be sacrificed")
    void cannotSacrificeIneligibleCreature() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new PhantasmalFiend());
        UUID ranger = harness.addToBattlefieldAndReturn(player1, new ElvishRanger()).getId();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ranger))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature artifact is not a legal sacrifice")
    void cannotSacrificeNoncreatureArtifact() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new PhantasmalFiend());
        UUID digger = harness.addToBattlefieldAndReturn(player1, new SoldeviDigger()).getId();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, digger))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Soldevi Digger");
    }

    @Test
    @DisplayName("Sacrificing a zero-mana-value artifact creature adds no mana")
    void sacrificeZeroManaValueArtifactCreatureAddsNoMana() {
        Permanent adnate = addCreatureReady(player1, new SoldeviAdnate());
        UUID shieldSphere = harness.addToBattlefieldAndReturn(player1, new ShieldSphere()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, shieldSphere);

        harness.assertInGraveyard(player1, "Shield Sphere");
        assertThat(adnate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent adnate = addCreatureReady(player1, new SoldeviAdnate());
        adnate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
