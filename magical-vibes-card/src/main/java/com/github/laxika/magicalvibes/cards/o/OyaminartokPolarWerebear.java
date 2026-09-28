package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceHasDealtDamage;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "68")
public class OyaminartokPolarWerebear extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Mystic Skyfish",
            "Moat Piranhas",
            "Riptide Turtle",
            "Spined Megalodon",
            "Ruin Crab",
            "Stinging Lionfish",
            "Archipelagore",
            "Pouncing Shoreshark",
            "Junk Winder",
            "Sigiled Starfish",
            "Sea-Dasher Octopus",
            "Voracious Greatshark",
            "Nadir Kraken",
            "Nezahal, Primal Tide",
            "Pursued Whale");

    public OyaminartokPolarWerebear() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceHasDealtDamage()),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.ofFoodToken(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.FOOD),
                                "Sacrifice a Food"),
                        new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(new AwardRestrictedManaEffect(
                                ManaColor.BLUE, 3, new ManaRestriction.CreatureSpells())))),
                "{U}, Sacrifice a Food: Draft a card from Oyaminartok's spellbook. When you do, add {U}{U}{U}. Spend this mana only to cast blue creature spells."
        ));
    }
}
