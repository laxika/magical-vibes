package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopyVista;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrineOfTheForsakenGods.class, CanopyVista.class, MindStone.class, SakuraTribeElder.class})
class ShrineOfTheForsakenGodsTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        addReadyShrine();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability requires seven lands")
    void secondAbilityRequiresSevenLands() {
        addReadyShrine();
        addLands(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 or more lands");
    }

    @Test
    @DisplayName("The second ability adds two mana restricted to colorless spells")
    void secondAbilityAddsColorlessSpellOnlyMana() {
        addReadyShrine();
        addLands(6);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless-spell-only mana can cast colorless spells")
    void manaCanCastColorlessSpells() {
        addReadyShrine();
        addLands(6);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.setHand(player1, List.of(new MindStone()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isZero();
    }

    @Test
    @DisplayName("Colorless-spell-only mana cannot cast colored spells")
    void manaCannotCastColoredSpells() {
        addReadyShrine();
        addLands(6);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new SakuraTribeElder()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isEqualTo(2);
    }

    private void addReadyShrine() {
        harness.addToBattlefieldAndReturn(player1, new ShrineOfTheForsakenGods()).setSummoningSick(false);
    }

    @Test
    @DisplayName("Opponents' lands do not satisfy the activation restriction")
    void opponentsLandsDoNotCount() {
        addReadyShrine();
        addLands(5);
        harness.addToBattlefield(player2, new CanopyVista());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 or more lands");
        assertThat(findPermanent(player1, "Shrine of the Forsaken Gods").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Nonland permanents do not satisfy the activation restriction")
    void nonlandPermanentsDoNotCount() {
        addReadyShrine();
        addLands(5);
        harness.addToBattlefield(player1, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 or more lands");
    }

    @Test
    @DisplayName("The restricted mana cannot pay for a colorless permanent's activated ability")
    void restrictedManaCannotPayForActivatedAbilities() {
        addReadyShrine();
        addLands(6);
        harness.addToBattlefield(player1, new MindStone());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 7, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    @DisplayName("The second ability is an immediate mana ability and taps the land")
    void secondAbilityResolvesImmediatelyAndCannotBeUsedAgainWhileTapped() {
        addReadyShrine();
        addLands(7);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Shrine of the Forsaken Gods").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getColorlessSpellOnlyMana()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new CanopyVista());
        }
    }
}
