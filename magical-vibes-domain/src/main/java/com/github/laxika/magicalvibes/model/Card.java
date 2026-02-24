package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
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
    @Setter private TargetFilter targetFilter;
    @Setter private Integer loyalty;
    @Setter private ManaColor xColorRestriction;
    @Setter private int minTargets;
    @Setter private int maxTargets;
    @Setter private String setCode;
    @Setter private String collectorNumber;
    @Setter private boolean shufflesIntoLibraryFromGraveyard;
    @Setter private boolean token;
    @Setter private boolean entersTapped;
    @Setter private List<TargetFilter> multiTargetFilters = List.of();
    @Setter private Card imprintedCard;

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

    public Set<TargetType> getAllowedTargets() {
        Set<TargetType> result = EnumSet.noneOf(TargetType.class);
        if (isAura()) {
            result.add(TargetType.PERMANENT);
        }
        for (CardEffect e : getEffects(EffectSlot.SPELL)) {
            collectTargetTypes(e, result);
        }
        for (CardEffect e : getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (e.canTargetPlayer()) result.add(TargetType.PLAYER);
            if (e.canTargetPermanent()) result.add(TargetType.PERMANENT);
        }
        return result;
    }

    private void collectTargetTypes(CardEffect e, Set<TargetType> out) {
        if (e.canTargetPlayer()) out.add(TargetType.PLAYER);
        if (e.canTargetPermanent()) out.add(TargetType.PERMANENT);
        if (e.canTargetSpell()) out.add(TargetType.SPELL_ON_STACK);
        if (e.canTargetGraveyard()) out.add(TargetType.GRAVEYARD);
    }

    public boolean isNeedsTarget() {
        Set<TargetType> t = getAllowedTargets();
        return t.contains(TargetType.PLAYER) || t.contains(TargetType.PERMANENT)
                || t.contains(TargetType.GRAVEYARD);
    }

    public boolean isNeedsSpellTarget() {
        return getAllowedTargets().contains(TargetType.SPELL_ON_STACK);
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
