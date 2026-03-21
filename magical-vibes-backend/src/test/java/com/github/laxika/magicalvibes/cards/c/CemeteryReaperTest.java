package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CemeteryReaperTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static boost effect for Zombies you control")
    void hasStaticBoostEffect() {
        CemeteryReaper card = new CemeteryReaper();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has activated ability to exile creature card from graveyard and create token")
    void hasActivatedAbility() {
        CemeteryReaper card = new CemeteryReaper();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}{B}");
        assertThat(ability.getEffects())
                .anyMatch(e -> e instanceof ExileTargetCardFromGraveyardEffect ex
                        && ex.requiredType() == CardType.CREATURE)
                .anyMatch(e -> e instanceof CreateCreatureTokenEffect);
    }

    // ===== Static effect: buffs own Zombies =====

    @Test
    @DisplayName("Other Zombie creatures you control get +1/+1")
    void buffsOwnZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cemetery Reaper does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent reaper = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cemetery Reaper"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CemeteryReaper());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does NOT buff opponent's Zombie creatures (you control only)")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player2, new Gravedigger());

        Permanent opponentZombie = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Cemetery Reapers buff each other")
    void twoReapersBuffEachOther() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player1, new CemeteryReaper());

        List<Permanent> reapers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cemetery Reaper"))
                .toList();

        assertThat(reapers).hasSize(2);
        for (Permanent reaper : reapers) {
            assertThat(gqs.getEffectivePower(gd, reaper)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, reaper)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Cemetery Reaper leaves the battlefield")
    void bonusRemovedWhenReaperLeaves() {
        harness.addToBattlefield(player1, new CemeteryReaper());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Cemetery Reaper"));

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(2);
    }

    // ===== Activated ability: exile creature card from graveyard + create token =====

    @Test
    @DisplayName("Exiles creature card from controller's graveyard and creates 2/2 Zombie token")
    void exilesCreatureAndCreatesToken() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Creature card exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // 2/2 black Zombie token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2
                        && p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Can exile creature card from opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        // Card exiled from opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Token created for controller (player1)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Rejects non-creature card as target")
    void rejectsNonCreatureTarget() {
        Permanent reaper = addReadyReaper(player1);
        Card cancel = new Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, cancel.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability taps Cemetery Reaper")
    void activatingTapsReaper() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThat(reaper.isTapped()).isFalse();

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        assertThat(reaper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent reaper = addReadyReaper(player1);
        reaper.tap();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        CemeteryReaper card = new CemeteryReaper();
        Permanent reaper = new Permanent(card);
        reaper.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(reaper);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);

        assertThatThrownBy(() -> harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target removed from graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);

        // Remove target before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        // No token created since exile fizzled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("Created Zombie token gets buffed by Cemetery Reaper's static ability")
    void createdTokenGetsBuffed() {
        Permanent reaper = addReadyReaper(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int reaperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reaper);
        harness.activateAbility(player1, reaperIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent zombieToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .findFirst().orElseThrow();

        // 2/2 base + 1/1 from Cemetery Reaper's lord effect = 3/3
        assertThat(gqs.getEffectivePower(gd, zombieToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombieToken)).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addReadyReaper(Player player) {
        CemeteryReaper card = new CemeteryReaper();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
