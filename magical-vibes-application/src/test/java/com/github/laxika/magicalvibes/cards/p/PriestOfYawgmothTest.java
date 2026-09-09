package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
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
        PriestOfYawgmoth.class,
        AesthirGlider.class,
        ElvishRanger.class,
        ShieldSphere.class,
        SoldeviDigger.class
})
class PriestOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact adds black mana equal to its mana value")
    void sacrificeArtifactAddsManaValueInBlack() {
        Permanent priest = addCreatureReady(player1, new PriestOfYawgmoth());
        addCreatureReady(player1, new AesthirGlider());
        addCreatureReady(player1, new SoldeviDigger());
        UUID glider = harness.getPermanentId(player1, "Aesthir Glider");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glider);

        harness.assertInGraveyard(player1, "Aesthir Glider");
        assertThat(priest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing a zero-mana-value artifact adds no mana")
    void sacrificeZeroManaValueArtifactAddsNoMana() {
        addCreatureReady(player1, new PriestOfYawgmoth());
        addCreatureReady(player1, new SoldeviDigger());
        UUID shieldSphere = harness.addToBattlefieldAndReturn(player1, new ShieldSphere()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, shieldSphere);

        harness.assertInGraveyard(player1, "Shield Sphere");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("A nonartifact permanent cannot be sacrificed")
    void cannotSacrificeNonartifactPermanent() {
        addCreatureReady(player1, new PriestOfYawgmoth());
        addCreatureReady(player1, new AesthirGlider());
        addCreatureReady(player1, new SoldeviDigger());
        UUID ranger = harness.addToBattlefieldAndReturn(player1, new ElvishRanger()).getId();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ranger))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature artifact can be sacrificed")
    void sacrificeNoncreatureArtifact() {
        addCreatureReady(player1, new PriestOfYawgmoth());
        harness.addToBattlefieldAndReturn(player1, new SoldeviDigger());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Soldevi Digger");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }
}
