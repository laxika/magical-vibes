package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagefireWings;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcanumWings.class, FertileGround.class, GrizzlyBears.class, MagefireWings.class})
class ArcanumWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Aura swap exchanges Arcanum Wings for an Aura from hand")
    void exchangesAuraForAuraFromHand() {
        Permanent creature = addCreature(new GrizzlyBears());
        addAura(creature, new ArcanumWings());
        harness.setHand(player1, List.of(new MagefireWings()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Arcanum Wings");
        harness.assertNotOnBattlefield(player1, "Arcanum Wings");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Magefire Wings")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(harness.getGameQueryService().getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining Aura swap leaves the source Aura attached")
    void decliningLeavesSourceAuraInPlay() {
        Permanent creature = addCreature(new GrizzlyBears());
        addAura(creature, new ArcanumWings());
        harness.setHand(player1, List.of(new MagefireWings()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Magefire Wings");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Wings")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Aura swap cannot choose an Aura that cannot enchant the source host")
    void cannotChooseAuraWithIncompatibleEnchantRestriction() {
        Permanent creature = addCreature(new GrizzlyBears());
        addAura(creature, new ArcanumWings());
        harness.setHand(player1, List.of(new FertileGround()));
        addAuraSwapMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fertile Ground");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Arcanum Wings")
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addAura(Permanent creature, Card card) {
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void addAuraSwapMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
