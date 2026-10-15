package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "342")
@CardRegistration(set = "MB2", collectorNumber = "580")
public class PenumbraUmbra extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("PenumbraUmbra", new OracleData(
                "Penumbra Umbra",
                CardType.ENCHANTMENT,
                Set.of(),
                "{1}{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.AURA),
                "Enchant creature you control\n"
                        + "When Penumbra Umbra is put into a graveyard from the battlefield, create a token "
                        + "that's a copy of enchanted creature, except the token is black.\n"
                        + "Umbra armor (If enchanted creature would be destroyed, instead remove all damage from it "
                        + "and destroy this Aura.)",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public PenumbraUmbra() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new TotemArmorEffect())
                .addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                        new CreateTokenCopyOfEnchantedPermanentEffect(
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(), Set.of(), null, null, Map.of(), CardColor.BLACK, Set.of())));
    }
}
