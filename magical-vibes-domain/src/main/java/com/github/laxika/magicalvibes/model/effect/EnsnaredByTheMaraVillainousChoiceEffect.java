package com.github.laxika.magicalvibes.model.effect;

/** Each opponent chooses between a free cast from their library and a four-card mana-value burn. */
public record EnsnaredByTheMaraVillainousChoiceEffect() implements CardEffect {

    public static final String CAST_OPTION = "You may cast the exiled card without paying its mana cost";
    public static final String DAMAGE_OPTION =
            "Exile the top four cards and take damage equal to their total mana value";
}
