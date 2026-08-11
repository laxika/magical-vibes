package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoggleTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Noggle the Mind attaches to a target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NoggleTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Noggle the Mind")
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature becomes a colorless 1/1 Noggle without abilities")
    void changesEnchantedCreature() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new NoggleTheMind());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectiveColors(gd, airElemental)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, airElemental))
                .containsExactly(CardSubtype.NOGGLE);
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing Noggle the Mind restores the creature")
    void removingAuraRestoresCreature() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new NoggleTheMind());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveColors(gd, airElemental)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, airElemental))
                .containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Noggle the Mind cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new NoggleTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
