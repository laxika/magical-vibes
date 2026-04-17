package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiManaManagerTest {

    @Mock
    private GameQueryService gameQueryService;

    private AiManaManager manager;

    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        manager = new AiManaManager(gameQueryService);
        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerIds.add(player1Id);
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ── Helper methods ──────────────────────────────────────────────

    private static Card createLand(String name, ManaColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(color));
        return card;
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createManaCreature(String name, ManaColor manaColor) {
        Card card = createCreature(name, 1, 1, CardColor.GREEN);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(manaColor));
        return card;
    }

    private static Card createDualLand(String name, ManaColor color1, ManaColor color2) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        // Dual land with two activated mana abilities (tap for either color)
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(color1)), "Add " + color1));
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(color2)), "Add " + color2));
        return card;
    }

    private static Card createPainLand(String name, ManaColor color1, ManaColor color2) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        // Colorless tap ability (no pain)
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(ManaColor.COLORLESS)), "Add {C}"));
        // Color ability with pain
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(color1), new DealDamageToControllerEffect(1)), "Add " + color1));
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardManaEffect(color2), new DealDamageToControllerEffect(1)), "Add " + color2));
        return card;
    }

    private static Card createAnyColorLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addEffect(EffectSlot.ON_TAP, new AwardAnyColorManaEffect());
        return card;
    }

    private Permanent addUntappedLand(ManaColor color) {
        return addUntappedLand("Land", color);
    }

    private Permanent addUntappedLand(String name, ManaColor color) {
        Card card = createLand(name, color);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    private Permanent addTappedLand(ManaColor color) {
        Card card = createLand("Tapped Land", color);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player1Id).add(perm);
        return perm;
    }

    private Permanent addUntappedCreature(String name, ManaColor manaColor) {
        Card card = createManaCreature(name, manaColor);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    private static Card createChargeCounterManaArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        // Tap + remove a charge counter: add one mana of any color (like Sphere of the Suns)
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new RemoveChargeCountersFromSourceCost(1), new AwardAnyColorManaEffect()),
                "{T}, Remove a charge counter: Add one mana of any color."));
        return card;
    }

    private Permanent addUntappedChargeCounterArtifact(String name, int chargeCounters) {
        Card card = createChargeCounterManaArtifact(name);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setChargeCounters(chargeCounters);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    private static Card createChargeCounterColorlessManaArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        // Tap, Sacrifice: Add {C} for each charge counter (like Shrine of Boundless Growth)
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SacrificeSelfCost(), new AddColorlessManaPerChargeCounterOnSourceEffect()),
                "{T}, Sacrifice: Add {C} for each charge counter."));
        return card;
    }

    private Permanent addUntappedChargeCounterColorlessManaArtifact(String name, int chargeCounters) {
        Card card = createChargeCounterColorlessManaArtifact(name);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setChargeCounters(chargeCounters);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    private static Card createMetalcraftManaArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.addActivatedAbility(new ActivatedAbility(
                true, null, List.of(new AwardAnyColorManaEffect()),
                "Metalcraft — {T}: Add one mana of any color.",
                ActivationTimingRestriction.METALCRAFT));
        return card;
    }

    private Permanent addUntappedMetalcraftArtifact(String name) {
        Card card = createMetalcraftManaArtifact(name);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    private Permanent addUntappedDualLand(String name, ManaColor color1, ManaColor color2) {
        Card card = createDualLand(name, color1, color2);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1Id).add(perm);
        lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
        lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
        lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
        return perm;
    }

    // ── buildVirtualManaPool ────────────────────────────────────────

    @Nested
    @DisplayName("buildVirtualManaPool")
    class BuildVirtualManaPool {

        @Test
        @DisplayName("empty battlefield returns empty pool")
        void emptyBattlefield() {
            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes existing mana in pool")
        void includesExistingMana() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 2);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
        }

        @Test
        @DisplayName("counts untapped basic land")
        void untappedBasicLand() {
            addUntappedLand(ManaColor.GREEN);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("skips tapped permanents")
        void skipsTapped() {
            addTappedLand(ManaColor.GREEN);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("skips summoning sick creature without haste")
        void skipsSummoningSickCreature() {
            Card card = createManaCreature("Llanowar Elves", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(true);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)).thenReturn(false);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes summoning sick creature with haste")
        void includesSummoningSickWithHaste() {
            Card card = createManaCreature("Hasty Elf", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(true);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("skips permanent that cannot activate mana abilities")
        void skipsCannotActivateMana() {
            Card card = createLand("Frozen Land", ManaColor.BLUE);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(false);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("uses overridden mana color when present")
        void usesOverriddenColor() {
            Card card = createLand("Plains", ManaColor.WHITE);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            // Evil Presence changed Plains to produce Black
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(ManaColor.BLACK);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
            assertThat(pool.get(ManaColor.WHITE)).isZero();
        }

        @Test
        @DisplayName("counts creature mana separately for mana creatures")
        void tracksCreatureMana() {
            addUntappedCreature("Llanowar Elves", ManaColor.GREEN);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("land does not add creature mana")
        void landDoesNotAddCreatureMana() {
            addUntappedLand(ManaColor.GREEN);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.getCreatureMana(ManaColor.GREEN)).isZero();
        }

        @Test
        @DisplayName("handles AwardAnyColorManaEffect as colorless")
        void anyColorTreatedAsColorless() {
            Card card = createAnyColorLand("City of Brass");
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("handles AwardAnyColorChosenSubtypeCreatureManaEffect as colorless")
        void chosenSubtypeManaAsColorless() {
            Card card = new Card();
            card.setName("Pillar of Origins");
            card.setType(CardType.ARTIFACT);
            card.addEffect(EffectSlot.ON_TAP, new AwardAnyColorChosenSubtypeCreatureManaEffect());
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("dual land adds both colors with flexible overcount")
        void dualLandAddsFlexibleOvercount() {
            addUntappedDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.getFlexibleOvercount()).isEqualTo(1);
        }

        @Test
        @DisplayName("multiple lands accumulate correctly")
        void multipleLands() {
            addUntappedLand("Forest", ManaColor.GREEN);
            addUntappedLand("Mountain", ManaColor.RED);
            addUntappedLand("Forest 2", ManaColor.GREEN);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.getTotal()).isEqualTo(3);
        }

        @Test
        @DisplayName("combines existing pool mana with battlefield mana")
        void combinesExistingAndBattlefield() {
            gd.playerManaPools.get(player1Id).add(ManaColor.BLUE, 1);
            addUntappedLand(ManaColor.BLUE);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(2);
        }

        @Test
        @DisplayName("pain land with three abilities adds overcount of 2")
        void painLandThreeAbilities() {
            Card card = createPainLand("Karplusan Forest", ManaColor.RED, ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            // 3 abilities, so flexible overcount = 3 - 1 = 2
            assertThat(pool.getFlexibleOvercount()).isEqualTo(2);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("dual land cannot pay both colored costs from a single tap")
        void dualLandCannotPayBothColors() {
            addUntappedDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            // Either color individually can be paid
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{R}").canPay(pool)).isTrue();
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{G}").canPay(pool)).isTrue();
            // But not {R}{G} — one tap can only produce one color
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{R}{G}").canPay(pool)).isFalse();
        }

        @Test
        @DisplayName("two duals can pay both colors with getTotal respecting each tap")
        void twoDualsCanPayBothColors() {
            addUntappedDualLand("Crag 1", ManaColor.RED, ManaColor.GREEN);
            addUntappedDualLand("Crag 2", ManaColor.RED, ManaColor.GREEN);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            // Each dual adds 2 raw but overcounts by 1 — effective total = 2 (one per tap)
            assertThat(pool.getTotal()).isEqualTo(2);
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{R}{G}").canPay(pool)).isTrue();
            // Still can't pay three colored mana though
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{R}{R}{G}").canPay(pool)).isFalse();
        }

        @Test
        @DisplayName("per-color overcount zero for duals and pain lands (each ability is distinct color)")
        void perColorOvercountZeroForDistinctColors() {
            addUntappedDualLand("Shivan Reef Lite", ManaColor.BLUE, ManaColor.RED);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getPerColorOvercount(ManaColor.BLUE)).isZero();
            assertThat(pool.getPerColorOvercount(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("per-color overcount tracks same-color inflation from multi-ability source")
        void perColorOvercountForSameColorAbilities() {
            // Hypothetical source with two abilities that both add Red.
            // Since the source only taps once, per-color overcount for Red should be 1
            // (2 red added, max from any one ability = 1).
            Card card = new Card();
            card.setName("Hypothetical Red Producer");
            card.setType(CardType.LAND);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.RED)), "Add R (a)"));
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.RED)), "Add R (b)"));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            VirtualManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getPerColorOvercount(ManaColor.RED)).isEqualTo(1);
            // Effective RED = 2 added - 1 overcount = 1
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.getTotal()).isEqualTo(1);
            // Cannot pay {R}{R} because the source only taps once
            assertThat(new com.github.laxika.magicalvibes.model.ManaCost("{R}{R}").canPay(pool)).isFalse();
        }

        @Test
        @DisplayName("copies existing creature mana from current pool")
        void copiesExistingCreatureMana() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 2);
            gd.playerManaPools.get(player1Id).addCreatureMana(ManaColor.GREEN, 1);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
            assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("overridden color on creature adds creature mana")
        void overriddenColorOnCreatureAddsCreatureMana() {
            Card card = createManaCreature("Dryad Arbor", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(ManaColor.BLACK);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
            assertThat(pool.getCreatureMana(ManaColor.BLACK)).isEqualTo(1);
            assertThat(pool.get(ManaColor.GREEN)).isZero();
        }

        @Test
        @DisplayName("AwardAnyColorManaEffect on creature tracks creature mana")
        void anyColorOnCreatureTracksCreatureMana() {
            Card card = createCreature("Exotic Bird", 0, 1, CardColor.GREEN);
            card.addEffect(EffectSlot.ON_TAP, new AwardAnyColorManaEffect());
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(pool.getCreatureMana(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("null battlefield returns pool based on existing mana only")
        void nullBattlefield() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 3);
            gd.playerBattlefields.remove(player1Id);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(3);
            assertThat(pool.getTotal()).isEqualTo(3);
        }

        @Test
        @DisplayName("activated ability with AwardAnyColorManaEffect adds colorless")
        void activatedAbilityAnyColorAddsColorless() {
            Card card = new Card();
            card.setName("Exotic Orchard");
            card.setType(CardType.LAND);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardAnyColorManaEffect()), "Add any"));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("creature with activated AwardAnyColorManaEffect tracks creature mana")
        void creatureActivatedAnyColorTracksCreatureMana() {
            Card card = createCreature("Mana Bird", 0, 1, CardColor.BLUE);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardAnyColorManaEffect()), "Add any"));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(pool.getCreatureMana(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("creature with activated mana ability tracks creature mana")
        void creatureWithActivatedAbilityTracksCreatureMana() {
            Card card = createCreature("Vessel of Volatility", 0, 1, CardColor.RED);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.RED, 2)), "Add RR"));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
            assertThat(pool.getCreatureMana(ManaColor.RED)).isEqualTo(2);
        }

        @Test
        @DisplayName("includes charge counter mana ability when permanent has enough counters")
        void includesChargeCounterAbilityWithCounters() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 3);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("excludes charge counter mana ability when permanent has zero counters")
        void excludesChargeCounterAbilityWithZeroCounters() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 0);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("charge counter artifact with exactly 1 counter is included")
        void chargeCounterArtifactWithExactlyOneCounter() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 1);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("mix of lands and depleted charge counter artifact only counts lands")
        void mixOfLandsAndDepletedArtifact() {
            addUntappedLand("Forest", ManaColor.GREEN);
            addUntappedChargeCounterArtifact("Sphere of the Suns", 0);
            addUntappedLand("Mountain", ManaColor.RED);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.get(ManaColor.COLORLESS)).isZero();
            assertThat(pool.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("includes colorless mana per charge counter ability with counters")
        void includesColorlessManaPerChargeCounterWithCounters() {
            addUntappedChargeCounterColorlessManaArtifact("Shrine of Boundless Growth", 3);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(3);
        }

        @Test
        @DisplayName("excludes colorless mana per charge counter ability with zero counters")
        void excludesColorlessManaPerChargeCounterWithZeroCounters() {
            addUntappedChargeCounterColorlessManaArtifact("Shrine of Boundless Growth", 0);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("colorless mana per charge counter combines with land mana")
        void colorlessManaPerChargeCounterCombinesWithLands() {
            addUntappedLand("Forest", ManaColor.GREEN);
            addUntappedChargeCounterColorlessManaArtifact("Shrine of Boundless Growth", 2);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
            assertThat(pool.getTotal()).isEqualTo(3);
        }

        @Test
        @DisplayName("excludes Metalcraft mana ability when player controls fewer than 3 artifacts")
        void excludesMetalcraftAbilityWithoutEnoughArtifacts() {
            addUntappedMetalcraftArtifact("Mox Opal");
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes Metalcraft mana ability when player controls 3 or more artifacts")
        void includesMetalcraftAbilityWithEnoughArtifacts() {
            addUntappedMetalcraftArtifact("Mox Opal");
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("mix of lands and Metalcraft artifact without Metalcraft only counts lands")
        void mixOfLandsAndMetalcraftArtifactWithoutMetalcraft() {
            addUntappedLand("Forest", ManaColor.GREEN);
            addUntappedMetalcraftArtifact("Mox Opal");
            addUntappedLand("Mountain", ManaColor.RED);
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.get(ManaColor.COLORLESS)).isZero();
            assertThat(pool.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("Metalcraft artifact with Metalcraft met adds mana alongside lands")
        void metalcraftArtifactWithMetalcraftMetAddsAlongsideLands() {
            addUntappedLand("Forest", ManaColor.GREEN);
            addUntappedMetalcraftArtifact("Mox Opal");
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(pool.getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("excludes Morbid mana ability when no creature died this turn")
        void excludesMorbidAbilityWithoutCreatureDeath() {
            Card card = new Card();
            card.setName("Morbid Rock");
            card.setType(CardType.ARTIFACT);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.BLACK)),
                    "Morbid — {T}: Add {B}.",
                    ActivationTimingRestriction.MORBID));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
            when(gameQueryService.isMorbidMet(gd)).thenReturn(false);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes Morbid mana ability when a creature died this turn")
        void includesMorbidAbilityWithCreatureDeath() {
            Card card = new Card();
            card.setName("Morbid Rock");
            card.setType(CardType.ARTIFACT);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.BLACK)),
                    "Morbid — {T}: Add {B}.",
                    ActivationTimingRestriction.MORBID));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
            when(gameQueryService.isMorbidMet(gd)).thenReturn(true);

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.BLACK)).isEqualTo(1);
        }

        @Test
        @DisplayName("excludes sorcery-speed mana ability during opponent's turn")
        void excludesSorcerySpeedAbilityDuringOpponentTurn() {
            UUID opponentId = UUID.randomUUID();
            Card card = new Card();
            card.setName("Sorcery Land");
            card.setType(CardType.LAND);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.RED)),
                    "{T}: Add {R}. Activate only as a sorcery.",
                    ActivationTimingRestriction.SORCERY_SPEED));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
            gd.activePlayerId = opponentId;
            gd.currentStep = TurnStep.PRECOMBAT_MAIN;

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes sorcery-speed mana ability during own main phase with empty stack")
        void includesSorcerySpeedAbilityDuringOwnMainPhase() {
            Card card = new Card();
            card.setName("Sorcery Land");
            card.setType(CardType.LAND);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.RED)),
                    "{T}: Add {R}. Activate only as a sorcery.",
                    ActivationTimingRestriction.SORCERY_SPEED));
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);
            lenient().when(gameQueryService.getOverriddenLandManaColor(gd, perm)).thenReturn(null);
            gd.activePlayerId = player1Id;
            gd.currentStep = TurnStep.PRECOMBAT_MAIN;

            ManaPool pool = manager.buildVirtualManaPool(gd, player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        }
    }

    // ── isFreeTapManaAbility ────────────────────────────────────────

    @Nested
    @DisplayName("isFreeTapManaAbility")
    class IsFreeTapManaAbility {

        @Test
        @DisplayName("tap + no mana cost + produces mana = true")
        void freeTapManaAbility() {
            ActivatedAbility ability = new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.GREEN)), "Add G");
            assertThat(AiManaManager.isFreeTapManaAbility(ability)).isTrue();
        }

        @Test
        @DisplayName("tap + has mana cost = false")
        void tapWithManaCost() {
            ActivatedAbility ability = new ActivatedAbility(
                    true, "{1}", List.of(new AwardManaEffect(ManaColor.GREEN)), "Add G");
            assertThat(AiManaManager.isFreeTapManaAbility(ability)).isFalse();
        }

        @Test
        @DisplayName("no tap + no mana cost = false")
        void noTapNoCost() {
            ActivatedAbility ability = new ActivatedAbility(
                    false, null, List.of(new AwardManaEffect(ManaColor.GREEN)), "Add G");
            assertThat(AiManaManager.isFreeTapManaAbility(ability)).isFalse();
        }

        @Test
        @DisplayName("tap + no mana cost + no mana effect = false")
        void tapNoCostNoManaEffect() {
            ActivatedAbility ability = new ActivatedAbility(
                    true, null, List.of(new DealDamageToControllerEffect(1)), "Ouch");
            assertThat(AiManaManager.isFreeTapManaAbility(ability)).isFalse();
        }

        @Test
        @DisplayName("AwardAnyColorManaEffect is recognized as mana producing")
        void anyColorManaAbility() {
            ActivatedAbility ability = new ActivatedAbility(
                    true, null, List.of(new AwardAnyColorManaEffect()), "Add any");
            assertThat(AiManaManager.isFreeTapManaAbility(ability)).isTrue();
        }
    }

    // ── tapLandsForCost ─────────────────────────────────────────────

    @Nested
    @DisplayName("tapLandsForCost")
    class TapLandsForCost {

        @Test
        @DisplayName("does not tap if pool already covers cost")
        void alreadyHasEnoughMana() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 3);
            addUntappedLand(ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForCost(gd, player1Id, "{1}{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("taps lands until cost is met")
        void tapsLandsUntilCostMet() {
            Permanent forest = addUntappedLand("Forest", ManaColor.GREEN);

            // Simulate mana being added after tap
            AiManaManager.ManaTapAction action = (permanentIndex, abilityIndex) -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
            };

            manager.tapLandsForCost(gd, player1Id, "{G}", 0, action);
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("skips tapped permanents")
        void skipsTappedPermanents() {
            addTappedLand(ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("skips summoning sick creatures without haste")
        void skipsSummoningSickCreatures() {
            Card card = createManaCreature("Elves", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(true);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)).thenReturn(false);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("uses null abilityIndex for ON_TAP mana effects")
        void usesNullAbilityIndexForOnTapEffects() {
            addUntappedLand("Forest", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            // Simulate the mana being added so cost is met
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(eq(0), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{G}", 0, action);

            verify(action).tap(0, null);
        }

        @Test
        @DisplayName("uses abilityIndex for activated mana abilities (dual land)")
        void usesAbilityIndexForActivatedAbilities() {
            addUntappedDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            // Simulate the mana being added so cost is met
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(0), any(Integer.class));

            manager.tapLandsForCost(gd, player1Id, "{R}", 0, action);

            // Should use ability index 0 (red) since we need red mana
            verify(action).tap(eq(0), eq(0));
        }

        @Test
        @DisplayName("handles null battlefield gracefully")
        void handlesNullBattlefield() {
            gd.playerBattlefields.remove(player1Id);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("pain land prefers painless colorless when only generic cost needed")
        void painLandPrefersColorlessForGenericCost() {
            Card card = createPainLand("Karplusan Forest", ManaColor.RED, ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);
                return null;
            }).when(action).tap(eq(0), any(Integer.class));

            // Cost is {1} — only generic, so painless colorless (index 0) should be preferred
            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            verify(action).tap(0, 0); // ability index 0 = colorless (no pain)
        }

        @Test
        @DisplayName("pain land prefers painful needed color over painless colorless for colored cost")
        void painLandPrefersNeededColorOverColorless() {
            Card card = createPainLand("Karplusan Forest", ManaColor.RED, ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            lenient().when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            lenient().when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(true);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(0), any(Integer.class));

            // Cost is {R} — needs red, so painful red (index 1) should be preferred over painless colorless
            manager.tapLandsForCost(gd, player1Id, "{R}", 0, action);

            verify(action).tap(0, 1); // ability index 1 = red (with pain, but needed)
        }

        @Test
        @DisplayName("respects costModifier")
        void respectsCostModifier() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
            gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            // Cost is {2}{G} but modifier is -1, so effective cost is {1}{G} = 2 total
            // We have 1G + 1C = 2 total, which should be enough
            manager.tapLandsForCost(gd, player1Id, "{2}{G}", -1, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("stops tapping when awaiting input is triggered by a mana ability")
        void stopsWhenAwaitingInput() {
            // Two lands: first tap triggers a color choice (sets awaiting input),
            // second land should NOT be tapped
            addUntappedLand("Forest 1", ManaColor.GREEN);
            addUntappedLand("Forest 2", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            // First tap simulates a mana ability that triggers awaiting input (e.g. Drover of the Mighty)
            lenient().doAnswer(invocation -> {
                gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
                return null;
            }).when(action).tap(eq(0), any());

            manager.tapLandsForCost(gd, player1Id, "{1}{G}", 0, action);

            verify(action).tap(eq(0), any());
            verify(action, never()).tap(eq(1), any());
        }

        @Test
        @DisplayName("continues tapping when ATTACKER_DECLARATION is already set (attack tax payment)")
        void continuesTappingDuringAttackerDeclaration() {
            // Simulate the attack tax scenario: game is already awaiting ATTACKER_DECLARATION
            // and the AI needs to tap multiple lands to pay the tax.
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            addUntappedLand("Island 1", ManaColor.BLUE);
            addUntappedLand("Island 2", ManaColor.BLUE);
            addUntappedLand("Island 3", ManaColor.BLUE);
            addUntappedLand("Island 4", ManaColor.BLUE);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.BLUE, 1);
                return null;
            }).when(action).tap(any(int.class), any());

            manager.tapLandsForCost(gd, player1Id, "{4}", 0, action);

            // All 4 lands should be tapped to pay the {4} cost
            verify(action).tap(eq(0), any());
            verify(action).tap(eq(1), any());
            verify(action).tap(eq(2), any());
            verify(action).tap(eq(3), any());
        }

        @Test
        @DisplayName("stops tapping when awaiting input changes during ATTACKER_DECLARATION")
        void stopsWhenAwaitingInputChangesDuringAttackerDeclaration() {
            // Game is already awaiting ATTACKER_DECLARATION, but a mana ability
            // triggers a COLOR_CHOICE — should bail out.
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            addUntappedLand("Land 1", ManaColor.BLUE);
            addUntappedLand("Land 2", ManaColor.BLUE);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
                return null;
            }).when(action).tap(eq(0), any());

            manager.tapLandsForCost(gd, player1Id, "{2}", 0, action);

            verify(action).tap(eq(0), any());
            verify(action, never()).tap(eq(1), any());
        }

        @Test
        @DisplayName("skips charge counter ability with zero counters during tapping")
        void skipsDepletedChargeCounterAbility() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 0);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("taps charge counter ability when permanent has enough counters")
        void tapsChargeCounterAbilityWithCounters() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 2);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);
                return null;
            }).when(action).tap(eq(0), eq(0));

            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            verify(action).tap(0, 0);
        }

        @Test
        @DisplayName("skips depleted artifact and taps land instead")
        void skipsDepletedArtifactTapsLandInstead() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 0);
            addUntappedLand("Forest", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            // Sphere at index 0 should be skipped, Forest at index 1 tapped
            verify(action, never()).tap(eq(0), any());
            verify(action).tap(1, null);
        }

        @Test
        @DisplayName("skips Metalcraft mana ability without Metalcraft and taps land instead")
        void skipsMetalcraftAbilityWithoutMetalcraftTapsLandInstead() {
            addUntappedMetalcraftArtifact("Mox Opal");
            addUntappedLand("Forest", ManaColor.GREEN);
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            // Mox Opal at index 0 should be skipped, Forest at index 1 tapped
            verify(action, never()).tap(eq(0), any());
            verify(action).tap(1, null);
        }

        @Test
        @DisplayName("taps Metalcraft mana ability when Metalcraft is met")
        void tapsMetalcraftAbilityWhenMetalcraftMet() {
            addUntappedMetalcraftArtifact("Mox Opal");
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(true);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);
                return null;
            }).when(action).tap(eq(0), eq(0));

            manager.tapLandsForCost(gd, player1Id, "{1}", 0, action);

            verify(action).tap(0, 0);
        }

        @Test
        @DisplayName("handles sacrifice mana ability that removes source from battlefield")
        void handlesSacrificeMidLoop() {
            // Shrine of Boundless Growth (sacrifice for mana) at index 0
            Permanent shrine = addUntappedChargeCounterColorlessManaArtifact("Shrine of Boundless Growth", 1);
            // Forest at index 1
            addUntappedLand("Forest", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);

            // When Forest is tapped (picked first for colored G demand), add green mana
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            // When Shrine is activated, it sacrifices itself and adds 1 colorless mana
            lenient().doAnswer(invocation -> {
                gd.playerBattlefields.get(player1Id).remove(shrine);
                gd.playerManaPools.get(player1Id).add(ManaColor.COLORLESS, 1);
                return null;
            }).when(action).tap(eq(0), eq(0));

            // Cost is {1}{G} — needs both colorless from Shrine and green from Forest
            manager.tapLandsForCost(gd, player1Id, "{1}{G}", 0, action);

            // Color-aware order: Forest tapped first (matches unmet G), then Shrine for generic.
            // Shrine sacrificing itself between iterations doesn't break the loop since each
            // iteration re-scans the (now-shorter) battlefield.
            verify(action).tap(1, null);
            verify(action).tap(0, 0);
        }

        @Test
        @DisplayName("prefers land producing unmet colored over basic of wrong color")
        void prefersMatchingColoredOverOtherBasic() {
            // [Island, Mountain] for cost {R} — Mountain should be picked even though it's later.
            addUntappedLand("Island", ManaColor.BLUE);
            addUntappedLand("Mountain", ManaColor.RED);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{R}", 0, action);

            verify(action).tap(1, null);
            verify(action, never()).tap(eq(0), any());
        }

        @Test
        @DisplayName("taps both Mountains for 1RR even when Islands precede them on battlefield")
        void tapsRightColorForDoubleRed() {
            // [Island, Island, Mountain, Mountain] for cost {1}{R}{R}.
            // Old slot-order iteration would tap Island, Island, Mountain and fail (R=1 < 2).
            // Color-aware picking should tap both Mountains first, then one Island for generic.
            addUntappedLand("Island 1", ManaColor.BLUE);
            addUntappedLand("Island 2", ManaColor.BLUE);
            addUntappedLand("Mountain 1", ManaColor.RED);
            addUntappedLand("Mountain 2", ManaColor.RED);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                int idx = invocation.getArgument(0);
                Permanent perm = gd.playerBattlefields.get(player1Id).get(idx);
                ManaColor color = perm.getCard().getName().startsWith("Mountain") ? ManaColor.RED : ManaColor.BLUE;
                gd.playerManaPools.get(player1Id).add(color, 1);
                return null;
            }).when(action).tap(any(int.class), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{1}{R}{R}", 0, action);

            // Both Mountains must be tapped for the RR requirement
            verify(action).tap(2, null);
            verify(action).tap(3, null);
            // And exactly one Island covers the generic 1
            ManaPool pool = gd.playerManaPools.get(player1Id);
            assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
            assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        }

        @Test
        @DisplayName("prefers specialized basic over dual land when both can pay colored")
        void prefersBasicOverDual() {
            // Rootbound Crag (R/G) at index 0, Mountain at index 1. Cost {R}.
            // Both can pay {R}, but Mountain is specialized so should be tapped to save the dual.
            addUntappedDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);
            addUntappedLand("Mountain", ManaColor.RED);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForCost(gd, player1Id, "{R}", 0, action);

            verify(action).tap(1, null);
            verify(action, never()).tap(eq(0), any());
        }
    }

    // ── tapCreaturesForCost ─────────────────────────────────────────

    @Nested
    @DisplayName("tapCreaturesForCost")
    class TapCreaturesForCost {

        @Test
        @DisplayName("only taps creatures, not lands")
        void onlyTapsCreatures() {
            addUntappedLand(ManaColor.GREEN);
            Card card = createManaCreature("Elf", ManaColor.GREEN);
            Permanent elf = new Permanent(card);
            elf.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(elf);
            when(gameQueryService.isCreature(gd, elf)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, elf)).thenReturn(true);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).addCreatureMana(ManaColor.GREEN, 1);
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            // Index 1 because the land is at index 0
            verify(action).tap(1, null);
        }

        @Test
        @DisplayName("does not tap if creature mana already covers cost")
        void alreadyHasEnoughCreatureMana() {
            gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
            gd.playerManaPools.get(player1Id).addCreatureMana(ManaColor.GREEN, 1);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("skips tapped creatures")
        void skipsTappedCreatures() {
            Card card = createManaCreature("Tapped Elf", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            perm.tap();
            gd.playerBattlefields.get(player1Id).add(perm);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("skips summoning sick creatures without haste")
        void skipsSummoningSickCreatures() {
            Card card = createManaCreature("Sick Elf", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(true);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)).thenReturn(false);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("skips creatures that cannot activate mana abilities")
        void skipsCreaturesCannotActivateMana() {
            Card card = createManaCreature("Locked Elf", ManaColor.GREEN);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(perm);
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.canActivateManaAbility(gd, perm)).thenReturn(false);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("handles null battlefield gracefully")
        void handlesNullBattlefield() {
            gd.playerBattlefields.remove(player1Id);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapCreaturesForCost(gd, player1Id, "{G}", 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("stops tapping when awaiting input is triggered by a mana ability")
        void stopsWhenAwaitingInput() {
            // Two mana creatures: first tap triggers a color choice, second should NOT be tapped
            addUntappedCreature("Drover 1", ManaColor.GREEN);
            addUntappedCreature("Drover 2", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
                return null;
            }).when(action).tap(eq(0), any());

            manager.tapCreaturesForCost(gd, player1Id, "{1}{G}", 0, action);

            verify(action).tap(eq(0), any());
            verify(action, never()).tap(eq(1), any());
        }

        @Test
        @DisplayName("continues tapping when ATTACKER_DECLARATION is already set (attack tax payment)")
        void continuesTappingDuringAttackerDeclaration() {
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            addUntappedCreature("Elf 1", ManaColor.GREEN);
            addUntappedCreature("Elf 2", ManaColor.GREEN);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).addCreatureMana(ManaColor.GREEN, 1);
                gd.playerManaPools.get(player1Id).add(ManaColor.GREEN, 1);
                return null;
            }).when(action).tap(any(int.class), any());

            manager.tapCreaturesForCost(gd, player1Id, "{G}{G}", 0, action);

            verify(action).tap(eq(0), any());
            verify(action).tap(eq(1), any());
        }
    }

    // ── tapLandsForXSpell ───────────────────────────────────────────

    @Nested
    @DisplayName("tapLandsForXSpell")
    class TapLandsForXSpell {

        @Test
        @DisplayName("does not tap if pool already covers X cost")
        void alreadyHasEnough() {
            gd.playerManaPools.get(player1Id).add(ManaColor.RED, 3);
            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForXSpell(gd, player1Id, xSpell, 2, 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("taps lands for X spell without color restriction")
        void xSpellWithoutColorRestriction() {
            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            addUntappedLand("Mountain", ManaColor.RED);
            addUntappedLand("Mountain 2", ManaColor.RED);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(any(int.class), eq(null));

            // xValue=1, so total cost is {1}{R} = 2 mana
            manager.tapLandsForXSpell(gd, player1Id, xSpell, 1, 0, action);

            verify(action).tap(0, null);
            verify(action).tap(1, null);
        }

        @Test
        @DisplayName("taps lands for X spell with color restriction")
        void xSpellWithColorRestriction() {
            Card xSpell = new Card();
            xSpell.setManaCost("{X}");
            xSpell.setXColorRestriction(ManaColor.RED);

            addUntappedLand("Mountain", ManaColor.RED);

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(0), eq(null));

            manager.tapLandsForXSpell(gd, player1Id, xSpell, 1, 0, action);

            verify(action).tap(0, null);
        }

        @Test
        @DisplayName("handles null battlefield gracefully")
        void handlesNullBattlefield() {
            gd.playerBattlefields.remove(player1Id);
            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            manager.tapLandsForXSpell(gd, player1Id, xSpell, 1, 0, action);

            verify(action, never()).tap(any(int.class), any());
        }

        @Test
        @DisplayName("stops tapping when awaiting input is triggered by a mana ability")
        void stopsWhenAwaitingInput() {
            // Three lands needed for {X}{R} with X=2, but first tap triggers awaiting input
            addUntappedLand("Mountain 1", ManaColor.RED);
            addUntappedLand("Mountain 2", ManaColor.RED);
            addUntappedLand("Mountain 3", ManaColor.RED);

            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
                return null;
            }).when(action).tap(eq(0), any());

            manager.tapLandsForXSpell(gd, player1Id, xSpell, 2, 0, action);

            verify(action).tap(eq(0), any());
            verify(action, never()).tap(eq(1), any());
            verify(action, never()).tap(eq(2), any());
        }

        @Test
        @DisplayName("continues tapping when ATTACKER_DECLARATION is already set (attack tax payment)")
        void continuesTappingDuringAttackerDeclaration() {
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            addUntappedLand("Mountain 1", ManaColor.RED);
            addUntappedLand("Mountain 2", ManaColor.RED);
            addUntappedLand("Mountain 3", ManaColor.RED);

            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(any(int.class), any());

            // xValue=2, so total cost is {2}{R} = 3 mana
            manager.tapLandsForXSpell(gd, player1Id, xSpell, 2, 0, action);

            verify(action).tap(eq(0), any());
            verify(action).tap(eq(1), any());
            verify(action).tap(eq(2), any());
        }

        @Test
        @DisplayName("skips depleted charge counter artifact for X spell")
        void skipsDepletedChargeCounterArtifactForXSpell() {
            addUntappedChargeCounterArtifact("Sphere of the Suns", 0);
            addUntappedLand("Mountain", ManaColor.RED);

            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForXSpell(gd, player1Id, xSpell, 0, 0, action);

            // Sphere at index 0 skipped, Mountain at index 1 tapped
            verify(action, never()).tap(eq(0), any());
            verify(action).tap(1, null);
        }

        @Test
        @DisplayName("skips Metalcraft mana ability without Metalcraft for X spell")
        void skipsMetalcraftAbilityWithoutMetalcraftForXSpell() {
            addUntappedMetalcraftArtifact("Mox Opal");
            addUntappedLand("Mountain", ManaColor.RED);
            when(gameQueryService.isMetalcraftMet(gd, player1Id)).thenReturn(false);

            Card xSpell = new Card();
            xSpell.setManaCost("{X}{R}");

            AiManaManager.ManaTapAction action = mock(AiManaManager.ManaTapAction.class);
            lenient().doAnswer(invocation -> {
                gd.playerManaPools.get(player1Id).add(ManaColor.RED, 1);
                return null;
            }).when(action).tap(eq(1), eq(null));

            manager.tapLandsForXSpell(gd, player1Id, xSpell, 0, 0, action);

            // Mox Opal at index 0 skipped, Mountain at index 1 tapped
            verify(action, never()).tap(eq(0), any());
            verify(action).tap(1, null);
        }
    }

    // ── calculateMaxAffordableX ─────────────────────────────────────

    @Nested
    @DisplayName("calculateMaxAffordableX")
    class CalculateMaxAffordableX {

        @Test
        @DisplayName("calculates max X from pool")
        void calculatesMaxX() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            // {X}{R} with 5R: pay 1R for the colored cost, 4 left for X
            int maxX = manager.calculateMaxAffordableX(card, pool, 0);
            assertThat(maxX).isEqualTo(4);
        }

        @Test
        @DisplayName("subtracts cost modifier")
        void subtractsCostModifier() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            int maxX = manager.calculateMaxAffordableX(card, pool, 1);
            assertThat(maxX).isEqualTo(3);
        }

        @Test
        @DisplayName("never returns negative")
        void neverNegative() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 1);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            int maxX = manager.calculateMaxAffordableX(card, pool, 5);
            assertThat(maxX).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("handles X color restriction")
        void handlesXColorRestriction() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 3);
            pool.add(ManaColor.GREEN, 2);

            Card card = new Card();
            card.setManaCost("{X}");
            card.setXColorRestriction(ManaColor.RED);

            // With red restriction, only red mana can be used for X
            int maxX = manager.calculateMaxAffordableX(card, pool, 0);
            assertThat(maxX).isEqualTo(3);
        }
    }

    // ── calculateSmartX ─────────────────────────────────────────────

    @Nested
    @DisplayName("calculateSmartX")
    class CalculateSmartX {

        @Test
        @DisplayName("returns 0 when maxX is 0")
        void zeroWhenNoMana() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 1);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            int x = manager.calculateSmartX(gd, card, null, pool, 0);
            assertThat(x).isZero();
        }

        @Test
        @DisplayName("returns maxX when no target")
        void maxXWhenNoTarget() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            int x = manager.calculateSmartX(gd, card, null, pool, 0);
            assertThat(x).isEqualTo(4);
        }

        @Test
        @DisplayName("caps X at creature toughness when targeting creature")
        void capsAtToughness() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 10);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            Card targetCard = createCreature("Bear", 2, 3, CardColor.GREEN);
            Permanent target = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(target);

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(3);

            int x = manager.calculateSmartX(gd, card, target.getId(), pool, 0);
            assertThat(x).isEqualTo(3);
        }

        @Test
        @DisplayName("uses maxX if target toughness exceeds available mana")
        void usesMaxXIfToughnessExceedsMana() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 3);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            Card targetCard = createCreature("Dragon", 5, 10, CardColor.RED);
            Permanent target = new Permanent(targetCard);
            gd.playerBattlefields.get(player1Id).add(target);

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.isCreature(gd, target)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(10);

            int x = manager.calculateSmartX(gd, card, target.getId(), pool, 0);
            // maxX = 2, toughness = 10, so X = min(10, 2) = 2
            assertThat(x).isEqualTo(2);
        }

        @Test
        @DisplayName("matches mana value for requiresManaValueEqualsX spells")
        void matchesManaValueForGraveyardReturn() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 5);

            Card card = new Card();
            card.setManaCost("{X}{B}");
            card.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .targetGraveyard(true)
                    .requiresManaValueEqualsX(true)
                    .build());

            Card graveyardCreature = createCreature("Bear", 2, 2, CardColor.GREEN);
            graveyardCreature.setManaCost("{1}{G}");
            gd.playerGraveyards.get(player1Id).add(graveyardCreature);

            when(gameQueryService.findCardInGraveyardById(gd, graveyardCreature.getId()))
                    .thenReturn(graveyardCreature);

            int x = manager.calculateSmartX(gd, card, graveyardCreature.getId(), pool, 0);
            // Bear has mana value 2, so X should be 2
            assertThat(x).isEqualTo(2);
        }

        @Test
        @DisplayName("returns 0 for requiresManaValueEqualsX when mana value exceeds maxX")
        void returnsZeroWhenManaValueExceedsMaxX() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 2);

            Card card = new Card();
            card.setManaCost("{X}{B}");
            card.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .targetGraveyard(true)
                    .requiresManaValueEqualsX(true)
                    .build());

            Card expensiveCreature = createCreature("Dragon", 5, 5, CardColor.RED);
            expensiveCreature.setManaCost("{4}{R}{R}");
            gd.playerGraveyards.get(player1Id).add(expensiveCreature);

            when(gameQueryService.findCardInGraveyardById(gd, expensiveCreature.getId()))
                    .thenReturn(expensiveCreature);

            int x = manager.calculateSmartX(gd, card, expensiveCreature.getId(), pool, 0);
            // Dragon has mana value 6, maxX = 1, so X = 0
            assertThat(x).isZero();
        }

        @Test
        @DisplayName("returns maxX when target is not a creature")
        void maxXWhenTargetNotCreature() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            UUID targetId = UUID.randomUUID();
            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            int x = manager.calculateSmartX(gd, card, targetId, pool, 0);
            assertThat(x).isEqualTo(4);
        }

        @Test
        @DisplayName("returns maxX when target permanent exists but is not a creature")
        void maxXWhenTargetIsNonCreaturePermanent() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.RED, 5);

            Card card = new Card();
            card.setManaCost("{X}{R}");

            Card artifactCard = new Card();
            artifactCard.setName("Sol Ring");
            artifactCard.setType(CardType.ARTIFACT);
            Permanent target = new Permanent(artifactCard);
            gd.playerBattlefields.get(player1Id).add(target);

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.isCreature(gd, target)).thenReturn(false);

            int x = manager.calculateSmartX(gd, card, target.getId(), pool, 0);
            assertThat(x).isEqualTo(4);
        }

        @Test
        @DisplayName("requiresManaValueEqualsX falls through when graveyard card not found")
        void requiresManaValueEqualsXGraveyardCardNotFound() {
            ManaPool pool = new ManaPool();
            pool.add(ManaColor.BLACK, 5);

            Card card = new Card();
            card.setManaCost("{X}{B}");
            card.addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .targetGraveyard(true)
                    .requiresManaValueEqualsX(true)
                    .build());

            UUID fakeTargetId = UUID.randomUUID();
            when(gameQueryService.findCardInGraveyardById(gd, fakeTargetId)).thenReturn(null);
            // After break, falls through to findPermanentById which also returns null → maxX
            when(gameQueryService.findPermanentById(gd, fakeTargetId)).thenReturn(null);

            int x = manager.calculateSmartX(gd, card, fakeTargetId, pool, 0);
            assertThat(x).isEqualTo(4);
        }
    }

    // ── addCardManaToPool ───────────────────────────────────────────

    @Nested
    @DisplayName("addCardManaToPool")
    class AddCardManaToPool {

        @Test
        @DisplayName("adds ON_TAP mana effect to pool")
        void addsOnTapMana() {
            Card card = createLand("Forest", ManaColor.GREEN);
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("adds any-color ON_TAP as colorless")
        void addsAnyColorAsColorless() {
            Card card = createAnyColorLand("City of Brass");
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("adds activated mana abilities for lands without ON_TAP effects")
        void addsActivatedAbilities() {
            Card card = createDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);
            VirtualManaPool pool = new VirtualManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
            assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(pool.getFlexibleOvercount()).isEqualTo(1);
        }

        @Test
        @DisplayName("adds AwardAnyColorChosenSubtypeCreatureManaEffect as colorless")
        void addsChosenSubtypeManaAsColorless() {
            Card card = new Card();
            card.setName("Pillar of Origins");
            card.setType(CardType.ARTIFACT);
            card.addEffect(EffectSlot.ON_TAP, new AwardAnyColorChosenSubtypeCreatureManaEffect());
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("handles card with no mana production")
        void handlesNonManaCard() {
            Card card = new Card();
            card.setName("Grizzly Bears");
            card.setType(CardType.CREATURE);
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.getTotal()).isZero();
        }

        @Test
        @DisplayName("includes charge counter mana ability for hypothetical card evaluation")
        void includesChargeCounterAbilityForHypotheticalEvaluation() {
            // addCardManaToPool is used for hypothetical evaluation (no permanent),
            // so charge counter costs should be assumed payable
            Card card = createChargeCounterManaArtifact("Sphere of the Suns");
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("includes Metalcraft mana ability for hypothetical card evaluation")
        void includesMetalcraftAbilityForHypotheticalEvaluation() {
            // addCardManaToPool is used for hypothetical evaluation (no game state),
            // so timing restrictions should be assumed met
            Card card = createMetalcraftManaArtifact("Mox Opal");
            ManaPool pool = new ManaPool();

            manager.addCardManaToPool(card, pool);

            assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        }
    }

    // ── getProducedColors ───────────────────────────────────────────

    @Nested
    @DisplayName("getProducedColors")
    class GetProducedColors {

        @Test
        @DisplayName("returns single color for basic land")
        void singleColorForBasicLand() {
            Card card = createLand("Forest", ManaColor.GREEN);

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactly(ManaColor.GREEN);
        }

        @Test
        @DisplayName("returns all colors for any-color land")
        void allColorsForAnyColorLand() {
            Card card = createAnyColorLand("City of Brass");

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactlyInAnyOrder(ManaColor.values());
        }

        @Test
        @DisplayName("returns both colors for dual land activated abilities")
        void bothColorsForDualLand() {
            Card card = createDualLand("Rootbound Crag", ManaColor.RED, ManaColor.GREEN);

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactlyInAnyOrder(ManaColor.RED, ManaColor.GREEN);
        }

        @Test
        @DisplayName("returns empty set for card with no mana production")
        void emptyForNonManaCard() {
            Card card = new Card();
            card.setName("Bear");
            card.setType(CardType.CREATURE);

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).isEmpty();
        }

        @Test
        @DisplayName("combines ON_TAP and activated ability colors")
        void combinesOnTapAndActivated() {
            Card card = new Card();
            card.setName("Special Land");
            card.setType(CardType.LAND);
            card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
            // This land also has an activated ability (unusual but testable)
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardManaEffect(ManaColor.BLUE)), "Add U"));

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactlyInAnyOrder(ManaColor.WHITE, ManaColor.BLUE);
        }

        @Test
        @DisplayName("returns all colors for activated AwardAnyColorManaEffect")
        void allColorsForActivatedAnyColorAbility() {
            Card card = new Card();
            card.setName("Exotic Orchard");
            card.setType(CardType.LAND);
            card.addActivatedAbility(new ActivatedAbility(
                    true, null, List.of(new AwardAnyColorManaEffect()), "Add any"));

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactlyInAnyOrder(ManaColor.values());
        }

        @Test
        @DisplayName("ignores non-free-tap activated abilities")
        void ignoresNonFreeTapAbilities() {
            Card card = new Card();
            card.setName("Land");
            card.setType(CardType.LAND);
            // Ability requires mana cost, not free
            card.addActivatedAbility(new ActivatedAbility(
                    true, "{1}", List.of(new AwardManaEffect(ManaColor.RED)), "Add R"));

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).isEmpty();
        }

        @Test
        @DisplayName("pain land returns all three colors including colorless")
        void painLandReturnsAllColors() {
            Card card = createPainLand("Karplusan Forest", ManaColor.RED, ManaColor.GREEN);

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactlyInAnyOrder(ManaColor.COLORLESS, ManaColor.RED, ManaColor.GREEN);
        }

        @Test
        @DisplayName("colorless mana per charge counter ability returns colorless")
        void colorlessManaPerChargeCounterReturnsColorless() {
            Card card = createChargeCounterColorlessManaArtifact("Shrine of Boundless Growth");

            Set<ManaColor> colors = manager.getProducedColors(card);

            assertThat(colors).containsExactly(ManaColor.COLORLESS);
        }
    }
}
