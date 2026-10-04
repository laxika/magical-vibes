package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.y.YotianMedic;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatDesertProspector.class, YotianMedic.class})
class GreatDesertProspectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one tapped Powerstone for each other creature you control")
    void createsTappedPowerstonesForOtherCreatures() {
        harness.addToBattlefield(player1, new YotianMedic());
        harness.addToBattlefield(player1, new YotianMedic());
        harness.addToBattlefield(player2, new YotianMedic());

        castProspector();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .toList();
        assertThat(powerstones).hasSize(2);
        assertThat(powerstones).allSatisfy(powerstone -> {
            assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(powerstone.getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
            assertThat(powerstone.isTapped()).isTrue();
        });
    }

    @Test
    @DisplayName("Powerstone mana is tracked as mana that cannot pay a nonartifact spell")
    void powerstoneManaUsesPowerstoneRestriction() {
        harness.addToBattlefield(player1, new YotianMedic());
        castProspector();

        Permanent powerstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .findFirst()
                .orElseThrow();
        powerstone.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("No Powerstones are created when only the Prospector is controlled")
    void createsNoPowerstonesWithoutOtherCreatures() {
        harness.addToBattlefield(player2, new YotianMedic());

        castProspector();

        assertThat(powerstones()).isEmpty();
    }

    @Test
    @DisplayName("Existing Powerstones are not counted as creatures")
    void ignoresNoncreaturePermanents() {
        harness.addToBattlefield(player1, new YotianMedic());
        castProspector();
        assertThat(powerstones()).hasSize(1);

        castProspector();

        assertThat(powerstones()).hasSize(3);
    }

    @Test
    @CardUsed(GoForTheThroat.class)
    @DisplayName("Creatures destroyed in response are not counted")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new YotianMedic());
        castProspectorLeavingTriggerOnStack();
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Yotian Medic"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yotian Medic");
        assertThat(powerstones()).isEmpty();
    }

    @Test
    @CardUsed(GoForTheThroat.class)
    @DisplayName("The trigger creates Powerstones even if the Prospector leaves")
    void triggerResolvesWithoutProspector() {
        harness.addToBattlefield(player1, new YotianMedic());
        castProspectorLeavingTriggerOnStack();
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Great Desert Prospector"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Great Desert Prospector");
        assertThat(powerstones()).hasSize(1).allSatisfy(powerstone -> assertThat(powerstone.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Powerstone mana cannot pay the generic cost of a nonartifact creature")
    void cannotSpendPowerstoneManaOnNonartifactSpell() {
        harness.addToBattlefield(player1, new YotianMedic());
        castProspector();
        Permanent powerstone = powerstones().getFirst();
        powerstone.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(powerstone), 0, null, null);
        harness.setHand(player1, List.of(new YotianMedic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0)).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Yotian Medic");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    private List<Permanent> powerstones() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .toList();
    }

    private void castProspector() {
        castProspectorLeavingTriggerOnStack();
        harness.passBothPriorities();
    }

    private void castProspectorLeavingTriggerOnStack() {
        harness.setHand(player1, List.of(new GreatDesertProspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
