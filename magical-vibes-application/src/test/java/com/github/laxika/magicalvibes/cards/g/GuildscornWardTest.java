package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BituminousBlast;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuildscornWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from multicolored sources")
    void enchantedCreatureHasProtectionFromMulticoloredSources() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castWard(bears);
        Permanent multicoloredSource = addCreatureReady(player2, new NivixGuildmage());

        assertThat(gqs.hasProtectionFromSource(gd, bears, multicoloredSource)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is not protected from monocolored sources")
    void enchantedCreatureIsNotProtectedFromMonocoloredSources() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castWard(bears);
        Permanent monocoloredSource = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, bears, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Multicolored spells cannot target the enchanted creature")
    void multicoloredSpellCannotTargetEnchantedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachWard(bears);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BituminousBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection is lost when Guildscorn Ward leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachWard(bears);
        Permanent multicoloredSource = addCreatureReady(player2, new NivixGuildmage());

        assertThat(gqs.hasProtectionFromSource(gd, bears, multicoloredSource)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFromSource(gd, bears, multicoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GuildscornWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castWard(Permanent host) {
        harness.setHand(player1, List.of(new GuildscornWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == GuildscornWard.class)
                .findFirst()
                .orElseThrow();
    }

    private Permanent attachWard(Permanent host) {
        Permanent aura = new Permanent(new GuildscornWard());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
