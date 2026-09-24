package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "52")
public class IshkanahBroodmother extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Twin-Silk Spider",
            "Drider",
            "Brood Weaver",
            "Glowstone Recluse",
            "Gnottvold Recluse",
            "Hatchery Spider",
            "Mammoth Spider",
            "Netcaster Spider",
            "Sentinel Spider",
            "Snarespinner",
            "Sporecap Spider",
            "Spidery Grasp",
            "Spider Spawning",
            "Prey Upon",
            "Arachnoform");

    public IshkanahBroodmother() {
        // Other Spiders you control get +1/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SPIDER)));

        // {1}{B/G}, Exile two cards from your graveyard: Draft a card from Ishkanah,
        // Broodmother's spellbook.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B/G}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)),
                "{1}{B/G}, Exile two cards from your graveyard: Draft a card from Ishkanah, "
                        + "Broodmother's spellbook."));
    }
}
