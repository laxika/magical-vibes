package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Card {

    private static final Map<String, OracleData> oracleRegistry = new ConcurrentHashMap<>();

    public static void registerOracle(String className, OracleData data) {
        oracleRegistry.put(className, data);
    }

    public static void clearOracleRegistry() {
        oracleRegistry.clear();
    }

    private final UUID id = UUID.randomUUID();
    @Setter private String name;
    @Setter private CardType type;
    @Setter private String manaCost;
    @Setter private CardColor color;

    @Setter private Set<CardType> additionalTypes = Set.of();
    @Setter private Set<CardSupertype> supertypes = Set.of();
    @Setter private List<CardSubtype> subtypes = List.of();
    @Setter private String cardText;
    @Setter private Integer power;
    @Setter private Integer toughness;
    @Setter private Set<Keyword> keywords = Set.of();
    @Setter private boolean needsTarget;
    @Setter private boolean needsSpellTarget;
    @Setter private TargetFilter targetFilter;
    @Setter private Integer loyalty;
    @Setter private ManaColor xColorRestriction;
    @Setter private int minTargets;
    @Setter private int maxTargets;
    @Setter private String setCode;
    @Setter private String collectorNumber;
    @Setter private boolean shufflesIntoLibraryFromGraveyard;

    private Map<EffectSlot, List<CardEffect>> effects = new EnumMap<>(EffectSlot.class);
    private List<ActivatedAbility> activatedAbilities = new ArrayList<>();

    public Card() {
        OracleData oracle = oracleRegistry.get(getClass().getSimpleName());
        if (oracle != null) {
            this.name = oracle.name();
            this.type = oracle.type();
            this.additionalTypes = oracle.additionalTypes();
            this.manaCost = oracle.manaCost();
            this.color = oracle.color();
            this.supertypes = oracle.supertypes();
            this.subtypes = oracle.subtypes();
            this.cardText = oracle.cardText();
            this.power = oracle.power();
            this.toughness = oracle.toughness();
            this.keywords = oracle.keywords();
            this.loyalty = oracle.loyalty();
        }
    }

    public List<CardEffect> getEffects(EffectSlot slot) {
        return effects.getOrDefault(slot, List.of());
    }

    public void addEffect(EffectSlot slot, CardEffect effect) {
        effects.computeIfAbsent(slot, k -> new ArrayList<>()).add(effect);
    }

    public void addActivatedAbility(ActivatedAbility ability) {
        activatedAbilities.add(ability);
    }

    public int getManaValue() {
        if (manaCost == null) return 0;
        return new ManaCost(manaCost).getManaValue();
    }

    public boolean isAura() {
        return subtypes.contains(CardSubtype.AURA);
    }

    public boolean isNeedsDamageDistribution() {
        boolean inSpell = getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect);
        boolean inAbility = activatedAbilities.stream()
                .flatMap(a -> a.getEffects().stream())
                .anyMatch(e -> e instanceof DealXDamageDividedAmongTargetAttackingCreaturesEffect);
        return inSpell || inAbility;
    }
}
