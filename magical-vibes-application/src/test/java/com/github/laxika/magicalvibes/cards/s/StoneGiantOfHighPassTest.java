package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneGiantOfHighPass.class, Spellbook.class, GrizzlyBears.class})
class StoneGiantOfHighPassTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/1 colorless Wall artifact creature token named Stone Boulder")
    void etbCreatesStoneBoulder() {
        harness.setHand(player1, List.of(new StoneGiantOfHighPass()));
        addStoneGiantMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent boulder = findPermanent(player1, "Stone Boulder");
        assertThat(boulder.getCard().getPower()).isEqualTo(3);
        assertThat(boulder.getCard().getToughness()).isEqualTo(1);
        assertThat(boulder.getCard().getColors()).isEmpty();
        assertThat(boulder.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(boulder.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(boulder.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(boulder.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Attacking creates a Stone Boulder token")
    void attackCreatesStoneBoulder() {
        addReadyStoneGiant();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Stone Boulder")).hasSize(1);
    }

    @Test
    @DisplayName("Ability sacrifices an artifact and deals 4 damage to a target player")
    void abilityDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new StoneGiantOfHighPass());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ability deals 4 damage to a target creature")
    void abilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new StoneGiantOfHighPass());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability cannot be activated without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new StoneGiantOfHighPass());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    private void addStoneGiantMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyStoneGiant() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new StoneGiantOfHighPass());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
